package com.tmukimi.hospital_management.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "doctors")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Audited
@SQLDelete(sql = "UPDATE doctors SET is_active = false WHERE id = ?")
@SQLRestriction("is_active = true")
public class Doctor extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String specialization;

    private String phone;

    @Column(nullable = false)
    private String roomNo;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private List<Appointment> appointments;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private List<DoctorSchedule> schedules;



    @Column(name = "is_active", nullable = false)
    private boolean active = true;




    private String degree;
    private String designation;
    private BigDecimal consultationFee = BigDecimal.valueOf(500.00);
    @Column(columnDefinition = "TEXT")
    private String aboutDoctor;
    private String gender;
    private String profilePictureUrl;
    private Integer experienceYears = 0;


    @Column(name = "is_profile_verified", nullable = false)
    @Builder.Default
    private boolean profileVerified = true;
}
