package com.aditya.simple_web_app.web_app.platform.workspace.controller;


import com.aditya.simple_web_app.web_app.auth.service.CustomUserDetails;
import com.aditya.simple_web_app.web_app.auth.service.WorkspaceService;
import com.aditya.simple_web_app.web_app.platform.workspace.domain.Workspace;
import com.aditya.simple_web_app.web_app.platform.workspace.dto.WorkspaceResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspace")
public class WorkspaceController {
    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }



    @GetMapping("/me")
    public ResponseEntity<WorkspaceResponseDTO> getCurrentWorkspace(
            @AuthenticationPrincipal CustomUserDetails customUserDetails
            ) {
        Workspace workspace = workspaceService.getWorkspaceForUser(customUserDetails.getUser());

        WorkspaceResponseDTO  workspaceResponseDTO = new WorkspaceResponseDTO(
                workspace.getId(),
                workspace.getName(),
                workspace.getSlug(),
                workspace.getPlan(),
                workspace.isActive(),
                workspace.getOwner().getId(),
                workspace.getOwner().getEmail()

        );
        return ResponseEntity.ok(workspaceResponseDTO);
    }
}
