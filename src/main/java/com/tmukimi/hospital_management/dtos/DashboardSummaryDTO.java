package com.tmukimi.hospital_management.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class DashboardSummaryDTO {
    private long totalPatients;
    private long totalDoctors;
    private long totalPrescriptions;
    private long totalAppointmentsToday;
    private BigDecimal totalRevenue;
}