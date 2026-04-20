package com.aditya.simple_web_app.web_app.platform.workspace.dto;

import java.util.UUID;

public record WorkspaceResponseDTO (
    UUID id,
    String name,
    String slug,
    PlanType plan,
    boolean active,
    UUID ownerId,
    String ownerEmail
)
{}

