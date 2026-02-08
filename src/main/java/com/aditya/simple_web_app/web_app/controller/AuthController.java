package com.aditya.simple_web_app.web_app.controller;

import com.aditya.simple_web_app.web_app.Domain.Role;
import com.aditya.simple_web_app.web_app.Domain.User;
import com.aditya.simple_web_app.web_app.dto.*;
import com.aditya.simple_web_app.web_app.service.UserRegistrationService;
import com.aditya.simple_web_app.web_app.util.TokenService;
import com.aditya.simple_web_app.web_app.util.TokenService.*;
import jakarta.validation.Valid;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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

import java.util.stream.Collectors;


@Validated
@RestController
@RequestMapping("/auth")
public class AuthController {


    private final UserRegistrationService userRegistrationService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final ApplicationEventPublisher applicationEventPublisher;
    public AuthController(UserRegistrationService userRegistrationService, AuthenticationManager authenticationManager, TokenService tokenService, ApplicationEventPublisher applicationEventPublisher) {
        this.userRegistrationService = userRegistrationService;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.applicationEventPublisher = applicationEventPublisher;
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
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO request)
    {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));


        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        String token = tokenService.generateToken(userDetails);

        return ResponseEntity.ok(new LoginResponseDTO(token));

    }
}
