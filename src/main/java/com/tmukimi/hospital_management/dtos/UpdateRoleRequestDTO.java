package com.tmukimi.hospital_management.dtos;

import com.tmukimi.hospital_management.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRoleRequestDTO {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "New role is required")
    private UserRole newRole;
}