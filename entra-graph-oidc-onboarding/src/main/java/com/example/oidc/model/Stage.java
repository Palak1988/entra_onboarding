package com.example.oidc.model;
import jakarta.persistence.*;
import java.sql.Timestamp;
//import java.util.List;
import lombok.*;   


@Entity
@Table(name = "stage")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stage;
    private String stageDesc;

    private Timestamp createdDate;
    private Timestamp updatedDate;
}

