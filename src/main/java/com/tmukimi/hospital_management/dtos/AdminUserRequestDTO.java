package com.tmukimi.hospital_management.dtos;

import com.tmukimi.hospital_management.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AdminUserRequestDTO {
    @Email(message = "Invalid email")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotNull(message = "Role is required")
    private UserRole role; // ADMIN, DOCTOR, or PATIENT

    private String name;
    private String specialization;

    private String degree;
    private String designation;
    private BigDecimal consultationFee;

    private String phone;
    private String roomNo;
    private Integer experienceYears;
    private String aboutDoctor;
}