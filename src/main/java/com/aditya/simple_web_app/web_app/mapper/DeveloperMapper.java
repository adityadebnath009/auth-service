package com.aditya.simple_web_app.web_app.mapper;

import com.aditya.simple_web_app.web_app.Domain.Developer;
import com.aditya.simple_web_app.web_app.dto.DeveloperResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeveloperMapper {

//    public Developer toDomain(CreateDeveloperRequest dev)
//    {
//        return new Developer(dev.)
//    }

    public DeveloperResponse toResponse(Developer dev)
    {
        return new DeveloperResponse(dev.getId(), dev.getName(), dev.getRole());
    }
    public List<DeveloperResponse> toResponseList(List<Developer> developers) {
        return developers.stream()
                .map(this::toResponse)
                .toList();
    }
}
