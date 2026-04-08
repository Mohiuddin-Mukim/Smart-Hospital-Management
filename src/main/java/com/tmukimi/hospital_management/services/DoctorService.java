package com.tmukimi.hospital_management.services;

import com.tmukimi.hospital_management.dtos.AdminUserRequestDTO;
import com.tmukimi.hospital_management.dtos.DoctorProfileUpdateDTO;
import com.tmukimi.hospital_management.dtos.DoctorResponseDTO;
import com.tmukimi.hospital_management.entities.Doctor;
import com.tmukimi.hospital_management.entities.DoctorProfileRequest;
import com.tmukimi.hospital_management.entities.QueueToken;
import com.tmukimi.hospital_management.enums.QueueStatus;
import com.tmukimi.hospital_management.repositories.DoctorProfileRequestRepository;
import com.tmukimi.hospital_management.repositories.DoctorRepository;
import com.tmukimi.hospital_management.repositories.QueueTokenRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final FileStorageService fileStorageService;

    private final QueueTokenRepository tokenRepository;
    private final SimpMessagingTemplate messagingTemplate; // সকেট মেসেজ পাঠানোর টুল
    private final DoctorProfileRequestRepository requestRepository;

    public List<DoctorResponseDTO> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public DoctorResponseDTO getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        return mapToDTO(doctor);
    }

    private DoctorResponseDTO mapToDTO(Doctor doctor) {
        return DoctorResponseDTO.builder()
                .id(doctor.getId())
                .name(doctor.getName())
                .email(doctor.getUser().getEmail())
                .specialization(doctor.getSpecialization())
                .phone(doctor.getPhone())
                .roomNo(doctor.getRoomNo())
                .degree(doctor.getDegree())
                .designation(doctor.getDesignation())
                .consultationFee(doctor.getConsultationFee())
                .aboutDoctor(doctor.getAboutDoctor())
                .profilePictureUrl(doctor.getProfilePictureUrl())
                .experienceYears(doctor.getExperienceYears())
                .build();
    }





    @Transactional
    public void callNextPatient(Long tokenId) {
        QueueToken token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        token.setStatus(QueueStatus.INSIDE);
        tokenRepository.save(token);

        Long doctorId = token.getAppointment().getDoctor().getId();
        String topic = "/topic/doctor/" + doctorId;

        messagingTemplate.convertAndSend(topic, token.getSerialNo());
    }



    @Transactional
    public String updateDoctorProfile(String email, DoctorProfileUpdateDTO dto, MultipartFile file) throws IOException {
        Doctor doctor = doctorRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));

        DoctorProfileRequest request = new DoctorProfileRequest();
        request.setDoctorId(doctor.getId());
        request.setName(dto.getName());
        request.setSpecialization(dto.getSpecialization());
        request.setDegree(dto.getDegree());
        request.setDesignation(dto.getDesignation());
        request.setPhone(dto.getPhone());
        request.setRoomNo(dto.getRoomNo());
        request.setAboutDoctor(dto.getAboutDoctor());
        request.setConsultationFee(dto.getConsultationFee());
        request.setExperienceYears(dto.getExperienceYears());

        if (file != null && !file.isEmpty()) {
            String path = fileStorageService.saveFile(file);
            request.setProfilePictureUrl(path);
        } else {
            request.setProfilePictureUrl(doctor.getProfilePictureUrl());
        }

        requestRepository.save(request);

        return "Your profile update has been submitted for admin approval.";
    }



    public List<DoctorResponseDTO> getPendingDoctors() {
        return doctorRepository.findByProfileVerifiedFalse()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void approveDoctorProfile(Long requestId) {
        DoctorProfileRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        doctor.setName(request.getName());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setDegree(request.getDegree());
        doctor.setDesignation(request.getDesignation());
        doctor.setPhone(request.getPhone());
        doctor.setRoomNo(request.getRoomNo());
        doctor.setConsultationFee(request.getConsultationFee());
        doctor.setAboutDoctor(request.getAboutDoctor());
        doctor.setProfilePictureUrl(request.getProfilePictureUrl());
        doctor.setExperienceYears(request.getExperienceYears());

        doctor.setProfileVerified(true);
        doctor.setActive(true);

        doctorRepository.save(doctor);

        request.setProcessed(true);
        requestRepository.save(request);
    }


    public DoctorResponseDTO getDoctorProfileByEmail(String email) {
        Doctor doctor = doctorRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Doctor not found with email: " + email));

        return DoctorResponseDTO.builder()
                .id(doctor.getId())
                .name(doctor.getName())
                .email(doctor.getUser().getEmail())
                .specialization(doctor.getSpecialization())
                .degree(doctor.getDegree())
                .designation(doctor.getDesignation())
                .consultationFee(doctor.getConsultationFee())
                .experienceYears(doctor.getExperienceYears())
                .phone(doctor.getPhone())
                .roomNo(doctor.getRoomNo())
                .aboutDoctor(doctor.getAboutDoctor())
                .profilePictureUrl(doctor.getProfilePictureUrl())
                .build();
    }



    public List<DoctorProfileRequest> getPendingRequests() {
        return requestRepository.findByProcessedFalse();
    }


}