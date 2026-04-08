package com.tmukimi.hospital_management.controllers;

import com.tmukimi.hospital_management.dtos.AppointmentResponseDTO;
import com.tmukimi.hospital_management.dtos.DoctorScheduleRequestDTO;
import com.tmukimi.hospital_management.services.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/appointments")
@RequiredArgsConstructor
public class AdminAppointmentController {

    private final AppointmentService appointmentService;

    // GET http://localhost:8080/api/admin/appointments/all
    @GetMapping("/all")
    public ResponseEntity<List<AppointmentResponseDTO>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }


    // DELETE http://localhost:8080/api/admin/appointments/1
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.ok("Appointment deleted successfully by Admin");
    }


    // POST http://localhost:8080/api/admin/appointments/schedule/5
    @PostMapping("/schedule/{doctorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> setDoctorScheduleByAdmin(
            @PathVariable Long doctorId,
            @RequestBody List<DoctorScheduleRequestDTO> requests) {
        appointmentService.setDoctorSchedule(doctorId, requests);
        return ResponseEntity.ok("Doctor schedule updated by Admin");
    }





    // GET http://localhost:8080/api/admin/appointments/schedule/5
    @GetMapping("/schedule/{doctorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DoctorScheduleRequestDTO>> getDoctorSchedule(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getDoctorScheduleById(doctorId));
    }
}