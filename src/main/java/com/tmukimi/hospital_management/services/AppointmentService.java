package com.tmukimi.hospital_management.services;

import com.tmukimi.hospital_management.dtos.AppointmentRequestDTO;
import com.tmukimi.hospital_management.dtos.AppointmentResponseDTO;
import com.tmukimi.hospital_management.dtos.DoctorAvailabilityDTO;
import com.tmukimi.hospital_management.dtos.DoctorScheduleRequestDTO;
import com.tmukimi.hospital_management.entities.Appointment;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {

    /// PATIENT SIDE
    AppointmentResponseDTO bookAppointment(AppointmentRequestDTO request, String email);
    List<AppointmentResponseDTO> getPatientAppointments(String email);
    void cancelAppointment(Long appointmentId, String email);

    /// DOCTOR SIDE
    List<AppointmentResponseDTO> getAppointmentsForDoctor(String email, LocalDate date);
    List<AppointmentResponseDTO> getAppointmentsForDoctorByDate(String email, LocalDate date);



    void updateAppointmentStatus(Long appointmentId, String status);

    List<AppointmentResponseDTO> getPendingRequestsForDoctor(String email);
    List<AppointmentResponseDTO> getAllPendingRequestsForAdmin();

    /// PUBLIC/GENERAL
    List<DoctorAvailabilityDTO> getAvailableSlots(Long doctorId, LocalDate date);

    /// ADMIN SIDE
    List<AppointmentResponseDTO> getAllAppointments();
    void deleteAppointment(Long id);


    void setDoctorSchedule(Long doctorId, List<DoctorScheduleRequestDTO> scheduleRequests);



    void setMySchedule(String username, List<DoctorScheduleRequestDTO> scheduleRequests);




    List<DoctorScheduleRequestDTO> getDoctorScheduleById(Long doctorId);



    List<DoctorScheduleRequestDTO> getMySchedule(String email);
}