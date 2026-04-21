package com.tmukimi.hospital_management.controllers;

import com.tmukimi.hospital_management.dtos.DashboardSummaryDTO;
import com.tmukimi.hospital_management.repositories.AppointmentRepository;
import com.tmukimi.hospital_management.repositories.DoctorRepository;
import com.tmukimi.hospital_management.repositories.PatientRepository;
import com.tmukimi.hospital_management.repositories.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentRepository appointmentRepository;

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardSummaryDTO> getSummary() {
        BigDecimal revenue = appointmentRepository.calculateTotalRevenue();
        DashboardSummaryDTO summary = new DashboardSummaryDTO(
                patientRepository.count(),
                doctorRepository.count(),
                prescriptionRepository.count(),
                appointmentRepository.countByDate(LocalDate.now()),
                revenue != null ? revenue : BigDecimal.ZERO
        );
        return ResponseEntity.ok(summary);
    }



    @GetMapping("/doctor-performance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Object[]>> getPerformance() {
        return ResponseEntity.ok(prescriptionRepository.getDoctorPerformanceData());
    }



    @GetMapping("/appointment-trends")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAppointmentTrends() {
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(6);
        List<Object[]> results = appointmentRepository.getAppointmentCountLast7Days(sevenDaysAgo);

        List<Map<String, Object>> chartData = results.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("date", row[0].toString());
            map.put("count", row[1]);
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(chartData);
    }


    @GetMapping("/medicine-analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getMedicineAnalytics() {
        Map<String, Object> response = new HashMap<>();


        List<Object[]> medicineResults = prescriptionRepository.getTopMedicinesFromAudit();
        List<Map<String, Object>> medicines = medicineResults.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
//            map.put("name", "ID: " + row[0]);
            map.put("name", row[0]);
            map.put("count", row[1]);
            return map;
        }).collect(Collectors.toList());


        List<Object[]> diagnosisResults = prescriptionRepository.getTopDiagnosisFromAudit();
        List<Map<String, Object>> diagnosis = diagnosisResults.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("type", row[0]);
            map.put("count", row[1]);
            return map;
        }).collect(Collectors.toList());

        response.put("medicines", medicines);
        response.put("diagnosis", diagnosis);

        return ResponseEntity.ok(response);
    }


}