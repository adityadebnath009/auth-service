package com.aditya.simple_web_app.web_app.auth.service;

import com.aditya.simple_web_app.web_app.platform.workspace.dto.PlanType;
import com.aditya.simple_web_app.web_app.auth.Domain.User;
import com.aditya.simple_web_app.web_app.platform.workspace.domain.Workspace;
import com.aditya.simple_web_app.web_app.common.exception.ResourceNotFoundException;
import com.aditya.simple_web_app.web_app.platform.workspace.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;


@Service
public class WorkspaceService {
    private final WorkspaceRepository workspaceRepository;
    public WorkspaceService(WorkspaceRepository workspaceRepository) {
        this.workspaceRepository = workspaceRepository;
    }


    public void createDefaultWorkspaceForUser(User user) {

        String slug = generateWorkspaceSlug(user.getName(), user.getEmail());
        String name = buildWorkspaceName(user);

        Workspace workspace = Workspace.builder()
                .name(name)
                .slug(slug)
                .owner(user)
                .plan(PlanType.FREE)
                .active(true)
                .build();
        workspaceRepository.save(workspace);
    }


    private String buildWorkspaceName(User user) {
        if (user.getName() != null && !user.getName().trim().isEmpty()) {
            return user.getName() + "'s Workspace";
        }

        return user.getEmail().split("@")[0] + "'s Workspace";
    }



    public  String generateWorkspaceSlug(String name, String email) {

        String base = (name != null && !name.trim().isEmpty())
                ? name
                : email.split("@")[0];

        base = base.replaceAll("'s\\b", "");

        String slugBase = base.toLowerCase().trim()
                .replaceAll("[^a-z0-9]", " ")
                .replaceAll("\\s+", "-") + "-workspace";

        String slug = slugBase;
        int counter = 2;

        while (workspaceRepository.existsBySlug(slug)) {
            slug = slugBase + "-" + counter;
            counter++;
        }

        return slug;
    }

    public Workspace getWorkspaceForUser(User user) {
        return workspaceRepository.findByOwner(user)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found for user"));
    }




}
