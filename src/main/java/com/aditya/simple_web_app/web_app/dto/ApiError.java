package com.aditya.simple_web_app.web_app.dto;


import java.time.LocalDateTime;

public record ApiError(
        int status,         //HTTP StatusCode
        String message,     //Message about the
        String path,        //The path where the error has occurred
        LocalDateTime timestamp        //When the error has occurred

) {}
