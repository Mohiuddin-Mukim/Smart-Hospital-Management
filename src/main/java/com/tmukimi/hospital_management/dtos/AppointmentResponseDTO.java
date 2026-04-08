package com.tmukimi.hospital_management.dtos;

import com.tmukimi.hospital_management.enums.AppointmentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class AppointmentResponseDTO {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String specialization;
    private LocalDate date;
    private LocalTime time;
    private AppointmentStatus status;
    private String reason;

    private String paymentUrl;

    private Long tokenId;

    private BigDecimal fee;
    private Long prescriptionId;
}
