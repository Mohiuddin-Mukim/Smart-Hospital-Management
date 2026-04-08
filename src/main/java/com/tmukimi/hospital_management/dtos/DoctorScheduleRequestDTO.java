package com.tmukimi.hospital_management.dtos;

import lombok.*;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class DoctorScheduleRequestDTO {
//    private Long doctorId;
    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private int slotDuration;
    private int maxPatients;
    private boolean isActive;
}
