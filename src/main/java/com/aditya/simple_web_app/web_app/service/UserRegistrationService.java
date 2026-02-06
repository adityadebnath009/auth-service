package com.aditya.simple_web_app.web_app.service;

import com.aditya.simple_web_app.web_app.Domain.Role;
import com.aditya.simple_web_app.web_app.Domain.User;
import com.aditya.simple_web_app.web_app.exception.UserAlreadyExistedException;
import com.aditya.simple_web_app.web_app.repository.RoleRepository;
import com.aditya.simple_web_app.web_app.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;


@Service
public class UserRegistrationService implements UserService{


    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRegistrationService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerUser(String email, String password) {

        if(userRepository.findByEmail(email).isPresent())
        {
            throw new UserAlreadyExistedException("User Already Existed", HttpStatus.BAD_REQUEST);
        }
        User user = User
                .builder().
                email(email).
                password(passwordEncoder.encode(password)).
                enabled(true).
                build();

        Role userRole = roleRepository.findByName("ROLE_USER").get();
        user.getRoles().add(userRole);
        return userRepository.save(user);


    }

    @Override
    public User findByEmail(String email) {

        return userRepository.findByEmail(email).orElseThrow(()->new UsernameNotFoundException("NO_USER_EXISTED"));

    }

    @Override
    public void assignRole(User user, String roleName) {

        Role userRole = roleRepository.findByName(roleName).get();

        user.getRoles().add(userRole);
        userRepository.save(user);

    }
}
