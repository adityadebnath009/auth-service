package com.aditya.simple_web_app.web_app.auth.controller;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/home")

public class HomeController {

    @GetMapping("/")
    public String home()
    {
        return "Welcome to my World";
    }

    @GetMapping("/csrf")
    public CsrfToken getToken(HttpServletRequest request)
    {
        return (CsrfToken) request.getAttribute(CsrfToken.class.getName());

    }
    @GetMapping("/user")
    public String user(){
        return "Welcome to my World of Commons";
    }

    @GetMapping("/admin")
    public String admin(){
        return "Welcome to my World of Aliens";
    }

}
