package com.tmukimi.hospital_management.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorAvailabilityDTO {
    private LocalTime time;
    private Boolean isAvailable;
    private String sessionName;
}
