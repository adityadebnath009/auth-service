package com.aditya.simple_web_app.web_app.controller;


import com.aditya.simple_web_app.web_app.Domain.Developer;
import com.aditya.simple_web_app.web_app.dto.*;
import com.aditya.simple_web_app.web_app.exception.BadRequestException;
import com.aditya.simple_web_app.web_app.mapper.DeveloperMapper;
import com.aditya.simple_web_app.web_app.service.DeveloperService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.config.RepositoryConfigurationSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Validated
@RestController
@RequestMapping("/developer")
public class DeveloperController {

    private final DeveloperService service;
    private final DeveloperMapper developerMapper;



    public DeveloperController(DeveloperService service, DeveloperMapper developerMapper) {
        this.service = service;
        this.developerMapper = developerMapper;

    }



    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<DeveloperResponse> createDeveloper(@RequestBody @Valid CreateDeveloperRequest request)
    {
        Developer dev  = service.createDeveloper(request.name(), request.role());

        return ResponseEntity.created(URI.create("/developers/" + dev.getId())).body(
                developerMapper.toResponse(dev)
        );
    }


    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping
    public ResponseEntity<PaginatedResponse<DeveloperResponse>> getAllDevelopers(
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "5") @Min(1) int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    )
    {

        int page = offset/size;
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort );
        Page<Developer> developerPage = service.getAllDevelopers(pageable);

        List<DeveloperResponse> data = developerMapper.toResponseList(developerPage.getContent());

        PaginationMeta meta = new PaginationMeta(
                offset,
                size,
                (int) developerPage.getTotalElements(),
                developerPage.getTotalPages(),
                developerPage.hasNext(),
                developerPage.hasPrevious()
        );
        return ResponseEntity.status(HttpStatus.OK).body(new PaginatedResponse<>(data, meta));

    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<DeveloperResponse> getDeveloperById(
            @PathVariable @Min(1) Long id
    ) {
        Developer dev = service.getDeveloperById(id);


        return ResponseEntity.status(HttpStatus.OK).body(
                developerMapper.toResponse(dev)
        );
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeveloperId(
            @PathVariable @Min(1) Long id
    )
    {
       service.deleteDeveloperById(id);


        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<DeveloperResponse> updateDeveloperById(
            @PathVariable @Min(1) Long id,
            @RequestBody @Valid UpdateDeveloperRequest request
            )
    {
        Developer dev = service.updateDeveloper(id, request);

        return ResponseEntity.ok(developerMapper.toResponse(dev));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<DeveloperResponse> patchDeveloperById(
            @PathVariable @Min(1) Long id,
            @RequestBody  PatchDeveloperRequest request
    )
    {
        Developer dev = service.patchUpdate(id, request);
        return ResponseEntity.ok(developerMapper.toResponse(dev));
    }


}
