package com.aditya.simple_web_app.web_app.controller;

import com.aditya.simple_web_app.web_app.Domain.EmailVerificationToken;
import com.aditya.simple_web_app.web_app.Domain.RefreshToken;
import com.aditya.simple_web_app.web_app.Domain.Role;
import com.aditya.simple_web_app.web_app.Domain.User;
import com.aditya.simple_web_app.web_app.dto.*;
import com.aditya.simple_web_app.web_app.service.*;
import com.aditya.simple_web_app.web_app.util.TokenService;
import com.aditya.simple_web_app.web_app.util.TokenService.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
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

    private final EmailVerificationTokenService emailVerificationTokenService;
    public AuthController(UserRegistrationService userRegistrationService, AuthenticationManager authenticationManager, TokenService tokenService, ApplicationEventPublisher applicationEventPublisher, CustomUserDetailsService customUserDetailsService, RefreshTokenService refreshTokenService,EmailVerificationTokenService emailVerificationTokenService) {
        this.userRegistrationService = userRegistrationService;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.customUserDetailsService = customUserDetailsService;
        this.refreshTokenService = refreshTokenService;
        this.emailVerificationTokenService = emailVerificationTokenService;
    }

    @PostMapping(value = "/register")
    public ResponseEntity<UserRegisterResponseDTO> register(@RequestBody @Valid UserRegisterDTO userRegister)
    {
        User user = userRegistrationService.registerUser(userRegister.name(), userRegister.email(), userRegister.password());
        String rawToken = emailVerificationTokenService.generateToken(user);
        applicationEventPublisher.publishEvent(new UserCreatedEvent(user, rawToken));

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

    @GetMapping("/verify/{token}")
    public ResponseEntity<?> verifyEmail(@PathVariable String token) {

        Optional<EmailVerificationToken> verificationToken =
                emailVerificationTokenService.validateToken(token);

        if (verificationToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid or expired verification token.");
        }

        emailVerificationTokenService.markUsed(verificationToken.get());

        User user = verificationToken.get().getUser();
        userRegistrationService.verifyUser(user);

        return ResponseEntity.ok("Email verified successfully. You can now log in.");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO request, HttpServletResponse response, HttpServletRequest httpServletRequest)
    {

        //Here we create an unauthenticated token.
        // The Authentication Manager receives it, the authentication manager has a provider manager. What it actually does is:
        //- It has many authentication providers.
        //- It checks which authentication provider supports these credentials.
        //- It selects and acts like a delegator.
        //- The authentication provider calls the UserDetailsService, which fetches the data from the database and then verifies if it is valid, and then returns a new authenticated object.


        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (DisabledException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }


        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = tokenService.generateAccessToken(userDetails);
        String refreshToken = tokenService.generateRefreshToken(userDetails);


         refreshTokenService.createRefreshToken(userDetails.getUser(),refreshToken,httpServletRequest.getHeader("User-Agent"),httpServletRequest.getRemoteAddr());


        ResponseCookie refreshCookie = buildRefreshCookie(refreshToken, httpServletRequest, "Strict");


        response.addHeader("Set-Cookie", refreshCookie.toString());

        return ResponseEntity.ok(new LoginResponseDTO(accessToken));

    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refreshToken(
            HttpServletRequest request, HttpServletResponse response
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

            CustomUserDetails userDetails =
                    (CustomUserDetails) customUserDetailsService.loadUserByUsername(username);

            Optional<RefreshToken> storedToken =
                    Optional.ofNullable(refreshTokenService.validateRefreshToken(refreshToken));


            if (storedToken.isEmpty()) {//Checks if the refresh token is validated or not
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            //Revoke the refresh token,
            // and we are actually implementing the rotation refresh token concept.
            refreshTokenService.revokeToken(refreshToken);

            String newRefreshToken = tokenService.generateRefreshToken(userDetails);

            refreshTokenService.createRefreshToken(userDetails.getUser(),newRefreshToken,request.getHeader("User-Agent"),request.getRemoteAddr());

            String newAccessToken =
                    tokenService.generateAccessToken(userDetails);
            ResponseCookie cookie = buildRefreshCookie(newRefreshToken, request, "Strict");

            response.addHeader("Set-Cookie", cookie.toString());

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

        ResponseCookie cookie = clearRefreshCookie(request, "Strict");
        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAll(Authentication authentication, HttpServletResponse response, HttpServletRequest request) {

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        refreshTokenService.revokeAllUserTokens(userDetails.getUser());

        ResponseCookie cookie = clearRefreshCookie(request, "Strict");
        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.status(HttpStatus.OK).build();
    }


    @GetMapping("/sessions")
    public ResponseEntity<List<SessionDTO>> getAllSessions(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        List<RefreshToken> refreshTokenList = refreshTokenService.getAllTokens(userDetails.getUser());
        List<SessionDTO> sessionDTOList = new ArrayList<>();

        for (RefreshToken refreshToken : refreshTokenList) {
            sessionDTOList.add(new SessionDTO(
                    refreshToken.getSessionId(),
                    refreshToken.getDeviceName(),
                    refreshToken.getIpAddress(),
                    refreshToken.getCreatedDate()

            ));
        }

        return ResponseEntity.ok(sessionDTOList);
    }


    @DeleteMapping("/sessions/{sessionID}")
    public ResponseEntity<?> deleteSession(Authentication authentication, @PathVariable("sessionID") String sessionID) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        refreshTokenService.revokeBySessionId(UUID.fromString(sessionID),userDetails.getUser());
        return  ResponseEntity.status(HttpStatus.OK).build();
    }

    private ResponseCookie buildRefreshCookie(String token, HttpServletRequest request, String sameSite) {
        return ResponseCookie.from("refresh_token", token)
                .httpOnly(true)
                .secure(isSecureRequest(request))
                .path("/auth/refresh")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite(sameSite)
                .build();
    }

    private ResponseCookie clearRefreshCookie(HttpServletRequest request, String sameSite) {
        return ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(isSecureRequest(request))
                .path("/auth/refresh")
                .maxAge(0)
                .sameSite(sameSite)
                .build();
    }

    private boolean isSecureRequest(HttpServletRequest request) {
        return request.isSecure() || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));
    }




}
