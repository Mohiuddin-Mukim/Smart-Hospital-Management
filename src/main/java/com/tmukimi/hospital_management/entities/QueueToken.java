package com.tmukimi.hospital_management.entities;

import com.tmukimi.hospital_management.enums.QueueStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "queue_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QueueToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    private int serialNo;

    @Enumerated(EnumType.STRING)
    private QueueStatus status = QueueStatus.WAITING;

}
