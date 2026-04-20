package com.aditya.simple_web_app.web_app.auth.repository;

import com.aditya.simple_web_app.web_app.legacy.Developer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeveloperRepository extends JpaRepository<Developer, Long> {

}
