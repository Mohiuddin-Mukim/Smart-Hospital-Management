package com.tmukimi.hospital_management.controllers;

import com.tmukimi.hospital_management.dtos.AppointmentRequestDTO;
import com.tmukimi.hospital_management.dtos.AppointmentResponseDTO;
import com.tmukimi.hospital_management.dtos.DoctorAvailabilityDTO;
import com.tmukimi.hospital_management.services.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class PatientAppointmentController {

    private final AppointmentService appointmentService;


    // POST http://localhost:8080/api/v1/appointments/book?
    @PostMapping("/book")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentResponseDTO> bookAppointment(
            @Valid @RequestBody AppointmentRequestDTO request,
            java.security.Principal principal) {
        AppointmentResponseDTO response = appointmentService.bookAppointment(request, principal.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    // GET http://localhost:8080/api/appointments/my?
    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<AppointmentResponseDTO>> getMyAppointments(java.security.Principal principal) {
        return ResponseEntity.ok(appointmentService.getPatientAppointments(principal.getName()));
    }




    // PUT http://localhost:8080/api/v1/appointments/1/cancel?
    @PutMapping("/{id}/cancel")
    public ResponseEntity<String> cancelAppointment(
            @PathVariable Long id,
            java.security.Principal principal) {
        appointmentService.cancelAppointment(id, principal.getName());
        return ResponseEntity.ok("Appointment cancelled successfully");
    }


    // GET http://localhost:8080/api/appointments/slots?doctorId=1&date=2026-02-20
    @GetMapping("/slots")
    public ResponseEntity<List<DoctorAvailabilityDTO>> getSlots(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(appointmentService.getAvailableSlots(doctorId, date));
    }
}