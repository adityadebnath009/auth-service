package com.aditya.simple_web_app.web_app.service;

import com.aditya.simple_web_app.web_app.Domain.Developer;
import com.aditya.simple_web_app.web_app.dto.PatchDeveloperRequest;
import com.aditya.simple_web_app.web_app.dto.UpdateDeveloperRequest;
import com.aditya.simple_web_app.web_app.exception.ResourceNotFoundException;
import com.aditya.simple_web_app.web_app.repository.DeveloperRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DeveloperService {

    private final DeveloperRepository repository;

    public DeveloperService(DeveloperRepository repository) {
        this.repository = repository;
    }

    public Developer createDeveloper(String name, String role) {
        Developer dev = new Developer(name, role);
        return repository.save(dev);
    }

    public Developer getDeveloperById(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No Developer found with ID " + id)
                );
    }

    public Page<Developer> getAllDevelopers(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public void deleteDeveloperById(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("No Developer found with ID " + id);
        }
        repository.deleteById(id);
    }



    public Developer updateDeveloper(Long id, UpdateDeveloperRequest request)
    {
        Developer dev = getDeveloperById(id);

        dev.setName(request.name());
        dev.setRole(request.role());

        return repository.save(dev);

    }

    public Developer patchUpdate(Long id, PatchDeveloperRequest request)
    {
        Developer dev = getDeveloperById(id);

        if(request.name()!=null)
        {
            dev.setName(request.name());
        }
        if(request.role()!=null)
        {
            dev.setRole(request.role());
        }

        return repository.save(dev);


    }
}
