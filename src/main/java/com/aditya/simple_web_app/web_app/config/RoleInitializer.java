package com.aditya.simple_web_app.web_app.config;


import com.aditya.simple_web_app.web_app.auth.Domain.Role;
import com.aditya.simple_web_app.web_app.auth.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class RoleInitializer {

    private final RoleRepository roleRepository;

    public RoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void populateRole()
    {

        if(!roleRepository.existsByName("ROLE_USER"))
        {
            roleRepository.save(new Role("ROLE_USER"));
        }
        if(!roleRepository.existsByName("ROLE_ADMIN"))
        {
            roleRepository.save(new Role("ROLE_ADMIN"));

        }
    }


}
