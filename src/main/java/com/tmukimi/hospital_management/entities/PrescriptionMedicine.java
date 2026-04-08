package com.tmukimi.hospital_management.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "prescription_medicines")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Audited
public class PrescriptionMedicine extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private MedicineBrand brand;

    @Column(length = 100)
    private String dosage;

    @Column(length = 50)
    private String duration;

    @Column(length = 255)
    private String instruction;

    @Column(nullable = false)
    private Integer days = 0;

    @Column(name = "is_continued", nullable = false)
    private boolean isContinued = false;
}