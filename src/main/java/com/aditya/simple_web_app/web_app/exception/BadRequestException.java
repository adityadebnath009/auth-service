package com.aditya.simple_web_app.web_app.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiException{

   public BadRequestException(String message, Throwable cause)
   {
       super(message, cause, HttpStatus.BAD_REQUEST);

    }
    public BadRequestException(String message)
    {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
