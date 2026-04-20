package com.aditya.simple_web_app.web_app.platform.workspace.repository;

import com.aditya.simple_web_app.web_app.auth.Domain.User;
import com.aditya.simple_web_app.web_app.platform.workspace.domain.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {

    Optional<Workspace> findByOwner(User owner);

    boolean existsBySlug(String slug);
}
