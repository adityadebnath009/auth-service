package com.aditya.simple_web_app.web_app.service;

import com.aditya.simple_web_app.web_app.Domain.User;
import com.aditya.simple_web_app.web_app.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;



@Service
public class CustomUserDetailsService implements UserDetailsService {


    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<User> userGet = Optional.of(userRepository
                .findByEmail(username)
                .orElseThrow(() ->
                {
                    throw new UsernameNotFoundException("USER_NOT_EXISTED");
                }));
        User user = userGet.get();



        return new CustomUserDetails(user);
    }
}
