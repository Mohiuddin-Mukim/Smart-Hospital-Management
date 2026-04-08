package com.tmukimi.hospital_management.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsDTO {
    private long totalAppointments;
    private long bookedCount;
    private long completedCount;
    private long cancelledCount;
}
