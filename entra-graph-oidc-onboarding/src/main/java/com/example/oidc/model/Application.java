package com.example.oidc.model;
import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.List;
import lombok.*;   


@Data
@Entity
@Table(name = "application")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itamId;
    private String itamInstanceId;
    private String sbiaRating;
    private String appAccessType;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private Status status;

    @ManyToOne
    @JoinColumn(name = "stage_id")
    private Stage stage;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL)
    private List<SamlConfig> samlConfigs;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL)
    private List<OpenIdConfig> openIdConfigs;

    private Timestamp createdDate;
    private Timestamp modifiedDate;

    // Other fields...
}

