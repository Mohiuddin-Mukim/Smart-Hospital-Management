package com.tmukimi.hospital_management.controllers;

import com.tmukimi.hospital_management.dtos.AdminUserRequestDTO;
import com.tmukimi.hospital_management.dtos.DoctorProfileUpdateDTO;
import com.tmukimi.hospital_management.dtos.DoctorResponseDTO;
import com.tmukimi.hospital_management.entities.PrescriptionMedicine;
import com.tmukimi.hospital_management.services.DoctorService;
import com.tmukimi.hospital_management.services.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/doctors")
public class DoctorContoller {

    private final DoctorService doctorService;
    private final PrescriptionService prescriptionService;

    // Public API: Anyone will be able to see the doctors list
    // GET http://localhost:8080/api/v1/doctors
    @GetMapping
    public ResponseEntity<List<DoctorResponseDTO>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    //Public API: to see a doctors profile
    // GET http://localhost:8080/api/v1/doctors/2
    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponseDTO> getDoctorById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }


    @PatchMapping("/tokens/{token-id}/call")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<String> callPatient(@PathVariable("token-id") Long tokenId) {
        doctorService.callNextPatient(tokenId);
        return ResponseEntity.ok("Patient called. Live status updated.");
    }



    @PutMapping(value = "/profile-update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<String> updateProfile(
            @RequestPart("dto") @Valid DoctorProfileUpdateDTO dto,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(doctorService.updateDoctorProfile(email, dto, file));
    }




    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<PrescriptionMedicine>> getPatientMeds(@PathVariable Long patientId) {
        return ResponseEntity.ok(prescriptionService.getPatientMedicineHistory(patientId));
    }





    @GetMapping("/me")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorResponseDTO> getMyProfile(Principal principal) {
        return ResponseEntity.ok(doctorService.getDoctorProfileByEmail(principal.getName()));
    }


}
