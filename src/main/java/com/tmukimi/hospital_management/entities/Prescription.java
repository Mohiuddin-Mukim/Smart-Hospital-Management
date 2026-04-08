package com.tmukimi.hospital_management.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDate;

@Entity
@Table(name = "prescriptions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Audited
public class Prescription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @Column(length = 10)
    private String weight;

    @Column(name = "blood_pressure", length = 20)
    private String bloodPressure;

    @Column(length = 10)
    private String temperature;

    @Column(length = 10)
    private String pulse;

    @Column(name = "chief_complaints", columnDefinition = "TEXT")
    private String chiefComplaints;

    @Column(name = "clinical_findings", columnDefinition = "TEXT")
    private String clinicalFindings;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String advice;

    @Column(name = "next_visit_date")
    private LocalDate nextVisitDate;

}
