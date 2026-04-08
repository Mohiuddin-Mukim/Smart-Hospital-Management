package com.tmukimi.hospital_management.entities;

import com.tmukimi.hospital_management.enums.BloodGroup;
import jakarta.persistence.*;

@Entity
@Table(name = "blood_requests")
public class BloodRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private BloodGroup bloodGroup;

    @Column(nullable = false)
    private String patientName;

    @Column(nullable = false)
    private String hospital;

    private String status;
}
