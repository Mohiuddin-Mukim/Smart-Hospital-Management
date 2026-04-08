package com.tmukimi.hospital_management.services;

import com.tmukimi.hospital_management.dtos.*;
import com.tmukimi.hospital_management.entities.Doctor;
import com.tmukimi.hospital_management.entities.Patient;
import com.tmukimi.hospital_management.entities.User;
import com.tmukimi.hospital_management.enums.UserRole;
import com.tmukimi.hospital_management.repositories.DoctorRepository;
import com.tmukimi.hospital_management.repositories.PatientRepository;
import com.tmukimi.hospital_management.repositories.RefreshTokenRepository;
import com.tmukimi.hospital_management.repositories.UserRepository;
import com.tmukimi.hospital_management.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public String registerPatient(SignupRequestDTO dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists!");
        }

        User user = User.builder()
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .role(UserRole.PATIENT)
                .active(true)
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);

        Patient patient = new Patient();

        patient.setUser(savedUser);
        patient.setName(dto.getName());
        patient.setAge(dto.getAge());
        patient.setGender(dto.getGender());
        patient.setPhone(dto.getPhone());
        patient.setAddress(dto.getAddress());


        patientRepository.save(patient);

        return "Patient registered successfully!";
    }


    public AuthResponseDTO login(AuthRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole().name());
        String refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponseDTO(accessToken, refreshToken, user.getId(), user.getEmail(), user.getRole().name());
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
    }

    public AuthResponseDTO refreshAccessToken(String refreshToken) {
        return refreshTokenRepository.findByToken(refreshToken)
                .filter(token -> !token.isRevoked() && token.getExpiryDate().isAfter(LocalDateTime.now()))
                .map(token -> {
                    User user = token.getUser();
                    String newAccessToken = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole().name());
                    return new AuthResponseDTO(newAccessToken, refreshToken, user.getId(), user.getEmail(), user.getRole().name());
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is invalid or expired!"));
    }





    @Transactional
    public String createUserByAdmin(AdminUserRequestDTO dto, MultipartFile file) throws IOException {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("User with this email already exists!");
        }

        User user = User.builder()
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .active(true)
                .emailVerified(true)
                .build();

        User savedUser = userRepository.save(user);

        if (dto.getRole() == UserRole.DOCTOR) {
            String profilePicPath = fileStorageService.saveFile(file);

            Doctor doctor = Doctor.builder()
                    .user(savedUser)
                    .name(dto.getName())
                    .specialization(dto.getSpecialization())
                    .degree(dto.getDegree())
                    .designation(dto.getDesignation())
                    .consultationFee(dto.getConsultationFee() != null ? dto.getConsultationFee() : BigDecimal.valueOf(500.00))
                    .phone(dto.getPhone())
                    .roomNo(dto.getRoomNo())
                    .experienceYears(dto.getExperienceYears() != null ? dto.getExperienceYears() : 0)
                    .aboutDoctor(dto.getAboutDoctor())
                    .profilePictureUrl(profilePicPath)
                    .active(true)
                    .build();

            doctorRepository.save(doctor);
        }
        return "Doctor created successfully with profile picture.";
    }




    @Transactional
    public String updateUserRole(UpdateRoleRequestDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + dto.getUserId()));

        user.setRole(dto.getNewRole());

        userRepository.save(user);

        return "User " + user.getEmail() + " role updated to " + dto.getNewRole();
    }
}