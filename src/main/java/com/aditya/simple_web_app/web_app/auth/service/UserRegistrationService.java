package com.aditya.simple_web_app.web_app.auth.service;

import com.aditya.simple_web_app.web_app.auth.Domain.Role;
import com.aditya.simple_web_app.web_app.auth.Domain.User;
import com.aditya.simple_web_app.web_app.common.exception.UserAlreadyExistedException;
import com.aditya.simple_web_app.web_app.auth.repository.RoleRepository;
import com.aditya.simple_web_app.web_app.auth.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UserRegistrationService implements UserService{


    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final WorkspaceService workspaceService;

    public UserRegistrationService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, ApplicationEventPublisher eventPublisher, WorkspaceService workspaceService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
        this.workspaceService = workspaceService;
    }

    @Override
    public User registerUser(String name, String email, String password) {

        if(userRepository.findByEmail(email).isPresent())
        {
            throw new UserAlreadyExistedException("User Already Existed", HttpStatus.BAD_REQUEST);
        }
        User user = User
                .builder().
                name(name).
                email(email).
                password(passwordEncoder.encode(password)).
                enabled(false).
                emailVerified(false).
                build();

        Role userRole = roleRepository.findByName("ROLE_USER").get();
        user.getRoles().add(userRole);
        userRepository.save(user);

        workspaceService.createDefaultWorkspaceForUser(user);


        return user;


    }

    public void verifyUser(User user) {
        user.setEnabled(true);
        user.setEmailVerified(true);
        userRepository.save(user);
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
