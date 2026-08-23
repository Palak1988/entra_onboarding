package com.example.oidc.model;
import jakarta.persistence.*;
import java.sql.Timestamp;
//import java.util.List;
import lombok.*;   

@Data
@Entity
@Table(name = "iam_config")
public class IamConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String configName;
    private String configType;

    @Column(columnDefinition = "longtext")
    private String configData;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private Request request;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private Status status;

    private Timestamp createdDate;
    private Timestamp updatedDate;
}
