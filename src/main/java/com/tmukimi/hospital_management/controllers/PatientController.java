package com.tmukimi.hospital_management.controllers;

import com.tmukimi.hospital_management.dtos.QueueStatusDTO;
import com.tmukimi.hospital_management.dtos.QueueTokenResponseDTO;
import com.tmukimi.hospital_management.dtos.SignupRequestDTO;
import com.tmukimi.hospital_management.services.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/patients")
public class PatientController {

    private final PatientService patientService;

    @PutMapping("/profile-update")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<String> updateProfile(@Valid @RequestBody SignupRequestDTO dto) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();   // extracting logged in user's information from SecurityContextHolder
        String currentEmail = authentication.getName();    // current user's email
        return ResponseEntity.ok(patientService.updateProfileByEmail(currentEmail, dto));
    }


    @GetMapping("/{appointment-id}/token-status")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<QueueTokenResponseDTO> getQueueTokenStatus(
            @PathVariable("appointment-id") Long appointmentId,
            Principal principal
    ) {
        return ResponseEntity.ok(patientService.getTokenSerialInfo(appointmentId,  principal.getName()));
    }


    @GetMapping("/{appointment-id}/live-status")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<QueueStatusDTO> getLiveQueueStatus(
            @PathVariable("appointment-id") Long appointmentId) {
        return ResponseEntity.ok(patientService.getLiveStatus(appointmentId));
    }

}