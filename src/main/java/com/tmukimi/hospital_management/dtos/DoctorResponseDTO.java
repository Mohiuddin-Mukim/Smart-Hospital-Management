package com.tmukimi.hospital_management.dtos;

import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String specialization;
    private String degree;
    private String designation;
    private BigDecimal consultationFee;
    private String aboutDoctor;
    private String phone;
    private String roomNo;
    private String profilePictureUrl;
    private Integer experienceYears;
}