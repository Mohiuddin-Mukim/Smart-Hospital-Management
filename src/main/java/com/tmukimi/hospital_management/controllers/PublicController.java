package com.tmukimi.hospital_management.controllers;

import com.tmukimi.hospital_management.entities.Notice;
import com.tmukimi.hospital_management.entities.Prescription;
import com.tmukimi.hospital_management.repositories.PrescriptionRepository;
import com.tmukimi.hospital_management.services.NoticeService;
import com.tmukimi.hospital_management.services.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/public")
public class PublicController {

    private final NoticeService noticeService;
    private final PrescriptionRepository prescriptionRepository;

    @GetMapping("/active-notices")
    public ResponseEntity<List<Notice>> getActiveNotices() {
        return ResponseEntity.ok(noticeService.getAllActiveNotices());
    }


    @GetMapping("/verify/prescription/{token}")
    public String verifyPrescription(@PathVariable String token, Model model) {
        Prescription prescription = prescriptionRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid Verification Token"));

        model.addAttribute("prescription", prescription);
        return "public-verify-page"; // একটি সিম্পল HTML পেজ যেখানে প্রেসক্রিপশনের সারসংক্ষেপ থাকবে
    }
}