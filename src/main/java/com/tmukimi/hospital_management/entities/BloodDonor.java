package com.tmukimi.hospital_management.entities;

import com.tmukimi.hospital_management.enums.BloodGroup;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "blood_donors")
public class BloodDonor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    private BloodGroup bloodGroup;

    private String phone;
    private String location;

    @Column(nullable = false)
    private LocalDate lastDonationDate;
}
