package com.example.oidc.model;
import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.List;
import lombok.*;   


@Data
@Entity
@Table(name = "status")
public class Status {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String status;
    private String description;

    private Timestamp createdDate;
    private Timestamp updatedDate;

    // Relationships
    @OneToMany(mappedBy = "status")
    private List<Application> applications;

    @OneToMany(mappedBy = "status")
    private List<Request> requests;

    @OneToMany(mappedBy = "status")
    private List<IamConfig> iamConfigs;

    // Getters & Setters
}
