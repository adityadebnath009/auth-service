package com.aditya.simple_web_app.web_app.config;


import com.aditya.simple_web_app.web_app.Domain.Role;
import com.aditya.simple_web_app.web_app.repository.RoleRepository;
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
            roleRepository.save(new Role(null, "ROLE_USER"));
        }
        if(!roleRepository.existsByName("ROLE_ADMIN"))
        {
            roleRepository.save(new Role(null, "ROLE_ADMIN"));

        }
    }


}
