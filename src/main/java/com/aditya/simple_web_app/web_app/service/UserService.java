package com.aditya.simple_web_app.web_app.service;

import com.aditya.simple_web_app.web_app.Domain.User;

public interface UserService {


    User registerUser(String email, String password);
    User findByEmail(String email);

    void assignRole(User user, String roleName);
}
