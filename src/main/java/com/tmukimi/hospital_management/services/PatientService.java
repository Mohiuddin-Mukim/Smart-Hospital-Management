package com.tmukimi.hospital_management.services;

import com.tmukimi.hospital_management.dtos.QueueStatusDTO;
import com.tmukimi.hospital_management.dtos.QueueTokenResponseDTO;
import com.tmukimi.hospital_management.dtos.SignupRequestDTO;
import com.tmukimi.hospital_management.entities.Appointment;
import com.tmukimi.hospital_management.entities.Patient;
import com.tmukimi.hospital_management.entities.QueueToken;
import com.tmukimi.hospital_management.entities.QueueTracker;
import com.tmukimi.hospital_management.mappers.QueueTokenMapper;
import com.tmukimi.hospital_management.repositories.PatientRepository;
import com.tmukimi.hospital_management.repositories.QueueTokenRepository;
import com.tmukimi.hospital_management.repositories.QueueTrackerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patientRepository;
    private final QueueTokenMapper queueTokenMapper;
    private final QueueTokenRepository queueTokenRepository;
    private final QueueTrackerRepository trackerRepository;

    public void printAllPatients() {
        List<Patient> patients = patientRepository.findAll();
        for (Patient patient : patients) {
            System.out.println("Patient: " + patient.getName());
        }
    }





    @Transactional
    public String updateProfileByEmail(String email, SignupRequestDTO dto) {
        Patient patient = patientRepository.findAll().stream()
                .filter(p -> p.getUser().getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        patient.setName(dto.getName());
        patient.setAge(dto.getAge());
        patient.setGender(dto.getGender());
        patient.setPhone(dto.getPhone());
        patient.setAddress(dto.getAddress());

        patientRepository.save(patient);
        return "Profile updated successfully for: " + email;
    }





    public QueueTokenResponseDTO getTokenSerialInfo(Long appointmentId, String currentUserEmail){
        QueueToken token = queueTokenRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Queue token not found for this appointment"));

        String patientEmail = token.getAppointment().getPatient().getUser().getEmail();
        if(!patientEmail.equals(currentUserEmail)){
            try {
                throw new AccessDeniedException("You are not authorized to view this token!");
            } catch (AccessDeniedException e) {
                throw new RuntimeException(e);
            }
        }
        return queueTokenMapper.toDTO(token);
    }





    public QueueStatusDTO getLiveStatus(Long appointmentId) {
        QueueToken token = queueTokenRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        QueueTracker tracker = trackerRepository.findByDoctorAndDate(
                token.getAppointment().getDoctor(),
                token.getAppointment().getDate()
        ).orElseThrow(() -> new RuntimeException("Live tracker not found"));

        return new QueueStatusDTO(
                token.getSerialNo(),
                tracker.getCurrentlyServing(),
                token.getAppointment().getDoctor().getName(),
                token.getStatus().name()
        );
    }

}