package com.tmukimi.hospital_management.entities;

import com.tmukimi.hospital_management.enums.BillStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bills")
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private BillStatus status = BillStatus.UNPAID;

    private LocalDate date;
}
