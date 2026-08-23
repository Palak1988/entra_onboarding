package com.example.oidc.model;
import jakarta.persistence.*;
import java.sql.Timestamp;
//import java.util.List;
import lombok.*;   
 

@Data
@Entity
@Table(name = "openid_config")
public class OpenIdConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "application_id")
    private Application application;

    private String requireUserConsent;
    private String authType;

    @Column(columnDefinition = "longtext")
    private String mtlsCert;

    private String claimAttributes;
    private String redirectUrl;
    private String grantType;

    private Timestamp createdDate;
    private Timestamp modifiedDate;
}

