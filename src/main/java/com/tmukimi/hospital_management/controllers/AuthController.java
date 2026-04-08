package com.tmukimi.hospital_management.controllers;

import com.tmukimi.hospital_management.dtos.*;
import com.tmukimi.hospital_management.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<String> registerPatient(@Valid @RequestBody SignupRequestDTO dto) {
        return ResponseEntity.ok(authService.registerPatient(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.ok("Logged out successfully.");
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponseDTO> refreshAccessToken(@RequestParam String refreshToken) {
        return ResponseEntity.ok(authService.refreshAccessToken(refreshToken));
    }


    @PostMapping(value = "/admin/create-user", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> createUserByAdmin(
            @RequestPart("dto") @Valid AdminUserRequestDTO dto,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createUserByAdmin(dto, file));
    }


    @PatchMapping("/admin/update-role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateUserRole(@Valid @RequestBody UpdateRoleRequestDTO dto) {
        String message = authService.updateUserRole(dto);
        return ResponseEntity.ok(message);
    }
}