package com.tmukimi.hospital_management.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "doctor_profile_request")
public class DoctorProfileRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long doctorId;
    private String name;
    private String specialization;
    private String degree;
    private String designation;
    private String phone;
    private String roomNo;
    private BigDecimal consultationFee;
    private String aboutDoctor;
    private String profilePictureUrl;
    private Integer experienceYears;

    private LocalDateTime requestedAt = LocalDateTime.now();
    private boolean processed = false;
}