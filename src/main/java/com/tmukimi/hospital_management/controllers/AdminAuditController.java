package com.tmukimi.hospital_management.controllers;

import com.tmukimi.hospital_management.dtos.AuditLogResponseDTO;
import com.tmukimi.hospital_management.entities.PrescriptionMedicine;
import com.tmukimi.hospital_management.services.AuditService;
import com.tmukimi.hospital_management.services.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/audit")
@RequiredArgsConstructor
public class AdminAuditController {

    private final PrescriptionService prescriptionService;
    private final AuditService auditService;

    //to see the full history of medicine of a patient
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<PrescriptionMedicine>> getFullHistory(@PathVariable Long patientId) {
        return ResponseEntity.ok(prescriptionService.getPatientMedicineHistory(patientId));
    }


    @GetMapping("/medicine-log/{pmId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<?>> getMedicineAuditLogs(@PathVariable Long pmId) {
        return ResponseEntity.ok(prescriptionService.getMedicineAuditTrail(pmId));
    }



    @GetMapping("/global")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getGlobalLogs(
                                            @RequestParam(name = "page", defaultValue = "0") int page,
                                            @RequestParam(name = "size", defaultValue = "10") int size) {
        try {
            return ResponseEntity.ok(auditService.getGlobalAuditLogs(page, size));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }



    @GetMapping("/appointment/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogResponseDTO>> getAppointmentHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(auditService.getAppointmentRevisionHistory(id, page, 5));
    }


    @GetMapping("/user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogResponseDTO>> getUserAudit(@PathVariable Long id) {
        return ResponseEntity.ok(auditService.getUserRevisionHistory(id, 0, 10));
    }

    @GetMapping("/doctor/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogResponseDTO>> getDoctorAudit(@PathVariable Long id) {
        return ResponseEntity.ok(auditService.getDoctorRevisionHistory(id, 0, 10));
    }
}