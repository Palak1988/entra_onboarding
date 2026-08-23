package com.example.oidc.model;
import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.List;
import lombok.*;   


@Data
@Entity
@Table(name = "saml_config")
public class SamlConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "application_id")
    private Application application;

    private String authInitiationType;
    private String requestSigned;
    private String responseSigned;
    private String responseEncrypted;

    @Column(columnDefinition = "longtext")
    private String requestSignedCert;

    @Column(columnDefinition = "longtext")
    private String responseEncryptedCert;

    private String nameIdFormat;
    private String assertionConsumerServiceUrl;
    private String spEntityId;

    private Timestamp createdDate;
    private Timestamp modifiedDate;
}

