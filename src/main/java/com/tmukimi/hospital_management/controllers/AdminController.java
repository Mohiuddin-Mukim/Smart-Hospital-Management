package com.tmukimi.hospital_management.controllers;


import com.tmukimi.hospital_management.entities.DoctorProfileRequest;
import com.tmukimi.hospital_management.services.DoctorService;
import com.tmukimi.hospital_management.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final UserService userService;
    private final DoctorService doctorService;


    @DeleteMapping("/delete-user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User soft-deleted successfully.");
    }



    @GetMapping("/pending-profiles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DoctorProfileRequest>> getPendingProfiles() {
        return ResponseEntity.ok(doctorService.getPendingRequests());
    }



    @PatchMapping("/approve-profile/{doctorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> approveDoctor(@PathVariable Long doctorId) {
        doctorService.approveDoctorProfile(doctorId);
        return ResponseEntity.ok("Doctor profile approved successfully!");
    }
}