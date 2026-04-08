package com.tmukimi.hospital_management.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalTime;

@Data
@AllArgsConstructor
public class AvailableSlotDTO {
    private LocalTime time;
    private boolean isAvailable;
}
