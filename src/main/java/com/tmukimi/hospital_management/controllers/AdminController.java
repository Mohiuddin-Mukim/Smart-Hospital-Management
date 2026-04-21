package com.tmukimi.hospital_management.controllers;


import com.tmukimi.hospital_management.entities.DoctorProfileRequest;
import com.tmukimi.hospital_management.repositories.NoticeRepository;
import com.tmukimi.hospital_management.services.DoctorService;
import com.tmukimi.hospital_management.services.NoticeService;
import com.tmukimi.hospital_management.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final UserService userService;
    private final DoctorService doctorService;
    private final NoticeService noticeService;
    private final NoticeRepository noticeRepository;


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




    // AdminController.java এর ভেতরে যোগ করুন

    @PostMapping("/add-notice")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> addNotice(@RequestBody Map<String, String> payload) {
        // এখানে NoticeService কল করে ডেটা সেভ করবেন
        String actualContent = payload.get("content");
        noticeService.saveNotice(actualContent);
        return ResponseEntity.ok("Notice updated successfully!");
    }

    @DeleteMapping("/delete-notice/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.ok("Notice deleted!");
    }


    @GetMapping("/current-notice")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> getCurrentNotice() {
        return noticeRepository.findById(1L)
                .map(notice -> ResponseEntity.ok(notice.getContent()))
                .orElse(ResponseEntity.ok("")); // নোটিশ না থাকলে খালি স্ট্রিং
    }


}