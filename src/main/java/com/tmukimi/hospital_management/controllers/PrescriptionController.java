package com.tmukimi.hospital_management.controllers;

import com.tmukimi.hospital_management.dtos.PrescriptionRequestDTO;
import com.tmukimi.hospital_management.services.PrescriptionPdfService;
import com.tmukimi.hospital_management.services.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final PrescriptionPdfService pdfService;

    @PostMapping("/save")
    public ResponseEntity<?> savePrescription(@RequestBody PrescriptionRequestDTO dto) {
        try {
            prescriptionService.savePrescription(dto);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Prescription saved successfully and billing generated!"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred."));
        }
    }



    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadPrescription(@PathVariable Long id) {
        byte[] pdfContent = pdfService.generatePrescriptionPdf(id);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=prescription_" + id + ".pdf")
                .body(pdfContent);
    }
}