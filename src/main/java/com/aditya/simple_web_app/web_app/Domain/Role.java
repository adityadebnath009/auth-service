package com.aditya.simple_web_app.web_app.Domain;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Service;


@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = false)
    private String name; //ROLE_USER, ROLE_ADMIN

}
