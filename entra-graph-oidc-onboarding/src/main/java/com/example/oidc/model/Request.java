package com.example.oidc.model;
import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.List;
import lombok.*;   


@Data
@Entity
@Table(name = "request")
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itamId;
    private String trackingId;
    private String workitemId;
    private String type;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private Status status;

    @ManyToOne
    @JoinColumn(name = "stage_id")
    private Stage stage;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL)
    private List<IamConfig> iamConfigs;

    private Timestamp createdDate;
    private Timestamp modifiedDate;

    @Column(columnDefinition = "json")
    private String metadata;

    @Column(columnDefinition = "json")
    private String parameters;

    private int executedTimes;
    private String createdBy;
    private String modifiedBy;
}
