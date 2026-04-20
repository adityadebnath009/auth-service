package com.aditya.simple_web_app.web_app.auth.service;

import com.aditya.simple_web_app.web_app.auth.Domain.User;

public interface UserService {


    User registerUser(String name, String email, String password);
    User findByEmail(String email);

    void assignRole(User user, String roleName);
}
