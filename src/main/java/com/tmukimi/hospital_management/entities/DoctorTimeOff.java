package com.tmukimi.hospital_management.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "doctor_time_off", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"doctor_id","off_date"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorTimeOff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "off_date", nullable = false)
    private LocalDate offDate;

    private String reason;
}