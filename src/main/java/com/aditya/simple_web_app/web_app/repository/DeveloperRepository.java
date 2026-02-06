package com.aditya.simple_web_app.web_app.repository;

import com.aditya.simple_web_app.web_app.Domain.Developer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeveloperRepository extends JpaRepository<Developer, Long> {

}
