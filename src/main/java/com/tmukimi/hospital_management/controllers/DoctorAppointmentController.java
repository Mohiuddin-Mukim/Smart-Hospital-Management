package com.tmukimi.hospital_management.controllers;

import com.tmukimi.hospital_management.dtos.AppointmentResponseDTO;
import com.tmukimi.hospital_management.dtos.DoctorScheduleRequestDTO;
import com.tmukimi.hospital_management.services.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.time.LocalDate;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/doctor/appointments")
@RequiredArgsConstructor
public class DoctorAppointmentController {

    private final AppointmentService appointmentService;

    // GET http://localhost:8080/api/v1/doctor/appointments/today?doctorId=1&date=2026-02-17
    @GetMapping("/daily")
    public ResponseEntity<List<AppointmentResponseDTO>> getDailyAppointments(
            Principal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(appointmentService.getAppointmentsForDoctor(principal.getName(), date));
    }




    // GET http://localhost:8080/api/v1/doctor/appointments/by-date?date=2026-04-10
    @GetMapping("/by-date")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<AppointmentResponseDTO>> getAppointmentsByDate(
            Principal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<AppointmentResponseDTO> appointments = appointmentService.getAppointmentsForDoctorByDate(principal.getName(), date);
        return ResponseEntity.ok(appointments);
    }




    // PATCH http://localhost:8080/api/doctor/appointments/1/status?status=COMPLETED
    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        appointmentService.updateAppointmentStatus(id, status);
        return ResponseEntity.ok("Appointment status updated to " + status);
    }


    // POST http://localhost:8080/api/v1/doctor/appointments/schedule
    @PostMapping("/schedule")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<String> saveMySchedule(
            @RequestBody List<DoctorScheduleRequestDTO> requests,
            Principal principal) {
        appointmentService.setMySchedule(principal.getName(), requests);
        return ResponseEntity.ok("Your schedules updated successfully");
    }




    @GetMapping("/requests")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<AppointmentResponseDTO>> getAllPendingRequests(Principal principal) {
        List<AppointmentResponseDTO> requests = appointmentService.getPendingRequestsForDoctor(principal.getName());
        return ResponseEntity.ok(requests);
    }




    // GET http://localhost:8080/api/v1/doctor/appointments/schedule
    @GetMapping("/schedule")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<DoctorScheduleRequestDTO>> getMySchedule(Principal principal) {
        return ResponseEntity.ok(appointmentService.getMySchedule(principal.getName()));
    }
}
