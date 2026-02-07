package com.aditya.simple_web_app.web_app.controller;


import com.aditya.simple_web_app.web_app.Domain.Role;
import com.aditya.simple_web_app.web_app.Domain.User;
import com.aditya.simple_web_app.web_app.dto.LoggedInUserResponse;
import com.aditya.simple_web_app.web_app.service.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {


    @GetMapping("/me")
    public ResponseEntity<LoggedInUserResponse> getLoggedInUser(@AuthenticationPrincipal CustomUserDetails userDetails)
    {
       User user = userDetails.getUser();

       LoggedInUserResponse response = new LoggedInUserResponse(
               user.getId(),
               user.getEmail(),
               user.getRoles()
                       .stream().map(Role::getName).collect(Collectors.toSet()),
               user.getCreatedAt()

       );

       return ResponseEntity.ok(response);

    }


}
