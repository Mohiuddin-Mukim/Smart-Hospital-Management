package com.tmukimi.hospital_management.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DoctorProfileUpdateDTO {
    private String name;
    private String specialization;
    private String degree;
    private String designation;
    private String phone;
    private String roomNo;
    private String aboutDoctor;
    private BigDecimal consultationFee;
    private String gender;
    private Integer experienceYears;
    private String profilePictureUrl;
}