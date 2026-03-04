package com.aditya.simple_web_app.web_app.controller;

import com.aditya.simple_web_app.web_app.Domain.RefreshToken;
import com.aditya.simple_web_app.web_app.Domain.Role;
import com.aditya.simple_web_app.web_app.Domain.User;
import com.aditya.simple_web_app.web_app.dto.*;
import com.aditya.simple_web_app.web_app.service.CustomUserDetails;
import com.aditya.simple_web_app.web_app.service.CustomUserDetailsService;
import com.aditya.simple_web_app.web_app.service.RefreshTokenService;
import com.aditya.simple_web_app.web_app.service.UserRegistrationService;
import com.aditya.simple_web_app.web_app.util.TokenService;
import com.aditya.simple_web_app.web_app.util.TokenService.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;


@Validated
@RestController
@RequestMapping("/auth")
public class AuthController {


    private final UserRegistrationService userRegistrationService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CustomUserDetailsService customUserDetailsService;
    private final RefreshTokenService refreshTokenService;
    public AuthController(UserRegistrationService userRegistrationService, AuthenticationManager authenticationManager, TokenService tokenService, ApplicationEventPublisher applicationEventPublisher, CustomUserDetailsService customUserDetailsService, RefreshTokenService refreshTokenService) {
        this.userRegistrationService = userRegistrationService;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.customUserDetailsService = customUserDetailsService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping(value = "/register")
    public ResponseEntity<UserRegisterResponseDTO> register(@RequestBody @Valid UserRegisterDTO userRegister)
    {
        User user = userRegistrationService.registerUser(userRegister.email(),userRegister.password());

        UserRegisterResponseDTO response = new UserRegisterResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getRoles().
                        stream().
                        map(Role::getName).
                        collect(Collectors.toSet()),
                user.getCreatedAt()

        );



        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO request, HttpServletResponse response)
    {

        //Here we create an unauthenticated token.
        // The Authentication Manager receives it, it calls our custom user data service,
        // verifies the password, and if it is valid, then it returns a new authenticated token.
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));


        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();


        String accessToken = tokenService.generateAccessToken(userDetails);
        String refreshToken = tokenService.generateRefreshToken(userDetails);


         refreshTokenService.createRefreshToken(userDetails.getUser(),refreshToken);


        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true) // true in production (HTTPS)
                .path("/api/auth/refresh")
                .maxAge(7 * 24 * 60 * 60) // 7 days
                .sameSite("Strict")
                .build();


        response.addHeader("Set-Cookie", refreshCookie.toString());

        return ResponseEntity.ok(new LoginResponseDTO(accessToken));

    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refreshToken(
            HttpServletRequest request
    ) {

        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String refreshToken = Arrays.stream(cookies)
                .filter(cookie -> "refresh_token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try{
            String username = tokenService.extractUsernameFromRefreshToken(refreshToken);

            UserDetails userDetails =
                    customUserDetailsService.loadUserByUsername(username);

            Optional<RefreshToken> storedToken =
                    refreshTokenService.validateRefreshToken(refreshToken);

            if (storedToken.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String newAccessToken =
                    tokenService.generateAccessToken(userDetails);

            return ResponseEntity.ok(new LoginResponseDTO(newAccessToken));
        }
        catch (Exception ex)
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }



    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return ResponseEntity.ok().build();
        }


        Arrays.stream(cookies)
                .filter(cookie -> "refresh_token".equals(cookie.getName()))
                .findFirst()
                .ifPresent(cookie -> refreshTokenService.revokeToken(cookie.getValue()));

        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/auth")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAll(Authentication authentication, HttpServletResponse response) {

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        refreshTokenService.revokeAllUserTokens(userDetails.getUser());

        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/auth")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.status(HttpStatus.OK).build();
    }



}
