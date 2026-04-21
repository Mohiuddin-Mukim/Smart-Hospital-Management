package com.tmukimi.hospital_management.services;

import com.tmukimi.hospital_management.dtos.*;
import com.tmukimi.hospital_management.entities.*;
import com.tmukimi.hospital_management.enums.AppointmentStatus;
import com.tmukimi.hospital_management.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorScheduleRepository scheduleRepository;

    private final PaymentRepository paymentRepository;
    private final SSLCommerzService sslCommerzService;
    private final PrescriptionRepository prescriptionRepository;

    private final DoctorScheduleRepository doctorScheduleRepository;

    @Override
    @Transactional
    public AppointmentResponseDTO bookAppointment(AppointmentRequestDTO request, String email) {

        Patient patient = patientRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));


        String dayOfWeek = request.getDate().getDayOfWeek().name();
        List<DoctorSchedule> sessions =
                scheduleRepository.findAllByDoctorIdAndDayOfWeekAndIsActiveTrue(
                        doctor.getId(), dayOfWeek
                );

        if (sessions.isEmpty()) {
            throw new RuntimeException("Doctor is not available on " + dayOfWeek);
        }


        LocalTime requestedTime = request.getTime();

        DoctorSchedule selectedSession = sessions.stream()
                .filter(s -> !requestedTime.isBefore(s.getStartTime()) &&
                        requestedTime.isBefore(s.getEndTime()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid session selected"));


        for (int attempt = 0; attempt < 3; attempt++) {

            LocalDateTime timeout = LocalDateTime.now().minusMinutes(6);


            List<LocalTime> bookedSlots = appointmentRepository.findBookedStartTimes(
                    doctor.getId(),
                    request.getDate(),
                    selectedSession.getStartTime(),
                    selectedSession.getEndTime(),
                    timeout
            );


            LocalTime slotTime = selectedSession.getStartTime();
            LocalTime estimatedTime = null;

            while (slotTime.isBefore(selectedSession.getEndTime())) {

                if (!bookedSlots.contains(slotTime)) {
                    estimatedTime = slotTime;
                    break;
                }

                slotTime = slotTime.plusMinutes(selectedSession.getSlotDuration());
            }

            if (estimatedTime == null) {
                throw new RuntimeException("This session is fully booked");
            }

            Appointment appointment = Appointment.builder()
                    .patient(patient)
                    .doctor(doctor)
                    .date(request.getDate())
                    .time(estimatedTime)
                    .reason(request.getReason())
                    .status(AppointmentStatus.PENDING)
                    .fee(new BigDecimal("100.00"))
                    .build();

            try {
                Appointment savedAppt = appointmentRepository.save(appointment);         // changing from saveAndFlush to save


                String tranId = "TXN-" + savedAppt.getId() + "-" + System.currentTimeMillis();
                double fee = savedAppt.getFee().doubleValue();

                String paymentUrl = sslCommerzService.initiatePayment(
                        fee,
                        tranId,
                        patient.getName(),
                        email
                );


                Payment payment = Payment.builder()
                        .appointment(savedAppt)
                        .patient(patient)
                        .amount(savedAppt.getFee())
                        .transactionId(tranId)
                        .invoiceNo("INV-" + System.currentTimeMillis())
                        .status("INITIATED")
                        .paymentMethod("SSLCOMMERZ")
                        .createdAt(LocalDateTime.now())
                        .build();

                paymentRepository.save(payment);


                AppointmentResponseDTO response = mapToResponse(savedAppt);
                response.setPaymentUrl(paymentUrl);

                return response;

            } catch (Exception e) {
                log.error("Error saving appointment: ", e);
                if (attempt == 2) {
                    throw new RuntimeException("High traffic! Please try again.");
                }
            }
        }

        throw new RuntimeException("Booking failed. Please try again.");
    }











    @Override
    public List<AppointmentResponseDTO> getPatientAppointments(String email) {
        return appointmentRepository.findAllByPatientEmail(email)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }





    @Override
    @Transactional
    public void cancelAppointment(Long appointmentId, String email) {

        Appointment appointment = appointmentRepository.findByIdWithPatient(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));


        if (!appointment.getPatient().getUser().getEmail().equals(email)) {
            throw new RuntimeException("You are not authorized");
        }

        Payment payment = paymentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Payment details not found"));

        if ("SUCCESS".equals(payment.getStatus())) {
            boolean isRefunded = sslCommerzService.initiateSSLCommerzRefund(
                    payment.getProviderReference(), // SSL bank_tran_id
                    appointment.getFee(),
                    "Cancelled by Patient"
            );

            if (isRefunded) {
                payment.setStatus("REFUNDED");
                payment.setRefundedAt(LocalDateTime.now());
                paymentRepository.save(payment);
            } else {
                throw new RuntimeException("Refund failed through SSLCommerz!");
            }
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }





    // Entity থেকে ResponseDTO তে কনভার্ট করার হেল্পার মেথড
    private AppointmentResponseDTO mapToResponse(Appointment appointment) {
        AppointmentResponseDTO dto = AppointmentResponseDTO.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatient().getId())
                .patientName(appointment.getPatient().getName())
                .doctorId(appointment.getDoctor().getId())
                .doctorName(appointment.getDoctor().getName())
                .specialization(appointment.getDoctor().getSpecialization())
                .date(appointment.getDate())
                .time(appointment.getTime())
                .status(appointment.getStatus())
                .reason(appointment.getReason())
                .fee(appointment.getDoctor().getConsultationFee())    // last change
                .build();



        prescriptionRepository.findByAppointmentId(appointment.getId())
                .ifPresent(p -> dto.setPrescriptionId(p.getId()));

        if (appointment.getToken() != null) {
            dto.setTokenId(appointment.getToken().getId());
        }

        return dto;
    }





    @Override
    public List<AppointmentResponseDTO> getAppointmentsForDoctor(String email, LocalDate date) {
        Doctor doctor = doctorRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        return appointmentRepository.findByDoctorIdAndDate(doctor.getId(), date)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }



    @Override
    public List<AppointmentResponseDTO> getAppointmentsForDoctorByDate(String email, LocalDate date) {
        Doctor doctor = doctorRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        return appointmentRepository.findByDoctorIdAndDate(doctor.getId(), date)
                .stream()
                .map(this::mapToResponse) // আপনার বর্তমান ম্যাপার মেথড
                .collect(Collectors.toList());
    }





    @Override
    public List<AppointmentResponseDTO> getPendingRequestsForDoctor(String email) {
        List<Appointment> appointments = appointmentRepository.findPendingRequestsByDoctorEmail(email);

        return appointments.stream().map(appt -> {
            AppointmentResponseDTO dto = new AppointmentResponseDTO();
            dto.setId(appt.getId());

            if (appt.getPatient() != null) {
                dto.setPatientName(appt.getPatient().getName());
            }

            dto.setDate(appt.getDate());
            dto.setTime(appt.getTime());
            dto.setStatus(appt.getStatus());
            dto.setReason(appt.getReason());

            return dto;
        }).collect(Collectors.toList());
    }


    @Override
    public List<AppointmentResponseDTO> getAllPendingRequestsForAdmin() {
        // Repository থেকে সব PENDING স্ট্যাটাস ফিল্টার করা
        List<Appointment> appointments = appointmentRepository.findAllByStatusOrderByIdDesc(AppointmentStatus.PENDING);

        return appointments.stream().map(appt -> {
            AppointmentResponseDTO dto = new AppointmentResponseDTO();
            dto.setId(appt.getId());
            dto.setPatientName(appt.getPatient() != null ? appt.getPatient().getName() : "Unknown");
            // অ্যাডমিনের জন্য ডক্টরের নাম জানা জরুরি
            dto.setDoctorName(appt.getDoctor() != null ? appt.getDoctor().getName() : "N/A");
            dto.setDate(appt.getDate());
            dto.setTime(appt.getTime());
            dto.setStatus(appt.getStatus());
            dto.setReason(appt.getReason());
            return dto;
        }).collect(Collectors.toList());
    }










    @Override
    @Transactional
    public void updateAppointmentStatus(Long appointmentId, String status) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        Payment payment = paymentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Payment details not found for this appointment"));

        try {
            AppointmentStatus newStatus = AppointmentStatus.valueOf(status.toUpperCase());

            if (newStatus == AppointmentStatus.BOOKED) {
                if (!"SUCCESS".equals(payment.getStatus())) {
                    throw new RuntimeException("Cannot approve appointment. Payment status is: " + payment.getStatus());
                }
            }

            /// REFUND LOGIC
            else if (newStatus == AppointmentStatus.CANCELLED || newStatus == AppointmentStatus.REJECTED) {
                if ("SUCCESS".equals(payment.getStatus())) {


                    String refundRemark;
                    if (newStatus == AppointmentStatus.CANCELLED) {
                        refundRemark = "Cancelled by Patient";
                    } else if (newStatus == AppointmentStatus.REJECTED) {
                        refundRemark = "Rejected by Doctor/Admin";
                    } else {
                        refundRemark = "Refund initiated for status: " + newStatus;
                    }

//                    boolean isRefunded = sslCommerzService.initiateSSLCommerzRefund(
//                            payment.getProviderReference(),
//                            appointment.getFee(),
//                            refundRemark
//                    );
//
//                    if (isRefunded) {
//                        payment.setStatus("REFUNDED");
//                        payment.setRefundedAt(LocalDateTime.now());
//                        paymentRepository.save(payment);
//                    } else {
//                        throw new RuntimeException("Refund failed through SSLCommerz! Please try again later.");
//                    }

                    try {
                        boolean isRefunded = sslCommerzService.initiateSSLCommerzRefund(
                                payment.getProviderReference(),
                                appointment.getFee(),
                                refundRemark
                        );

                        if (isRefunded) {
                            payment.setStatus("REFUNDED");
                            payment.setRefundedAt(LocalDateTime.now());
                        } else {
                            // নতুন কলাম ছাড়াই স্ট্যাটাস দিয়ে মার্ক করে রাখা
                            payment.setStatus("FAILED");
                        }
                    } catch (Exception e) {
                        // গেটওয়ে এরর হলেও যেন অ্যাপয়েন্টমেন্ট রিজেক্ট হয়
                        payment.setStatus("FAILED");
                    }

                    paymentRepository.save(payment);

                }
            }

            appointment.setStatus(newStatus);
            appointmentRepository.save(appointment);

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status! Use: PENDING, BOOKED, COMPLETED, CANCELLED, etc.");
        }
    }





    @Override
    public List<DoctorAvailabilityDTO> getAvailableSlots(Long doctorId, LocalDate date) {

        String dayOfWeek = date.getDayOfWeek().name();
        List<DoctorSchedule> sessions =
                scheduleRepository.findAllByDoctorIdAndDayOfWeekAndIsActiveTrue(doctorId, dayOfWeek);

        List<DoctorAvailabilityDTO> availabilityList = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        LocalDateTime timeout = LocalDateTime.now().minusMinutes(6);

        for (DoctorSchedule session : sessions) {

            List<LocalTime> bookedSlots =
                    appointmentRepository.findBookedStartTimes(
                            doctorId,
                            date,
                            session.getStartTime(),
                            session.getEndTime(),
                            timeout
                    );

            LocalTime tempTime = session.getStartTime();
            String sessionDisplayName =
                    session.getStartTime() + " - " + session.getEndTime();

            while (tempTime.isBefore(session.getEndTime())) {

                boolean isBooked = bookedSlots.contains(tempTime);
                boolean isPast = date.isEqual(today) && tempTime.isBefore(now);

                availabilityList.add(
                        DoctorAvailabilityDTO.builder()
                                .time(tempTime)
                                .isAvailable(!isBooked && !isPast)
                                .sessionName(sessionDisplayName)
                                .build()
                );

                tempTime = tempTime.plusMinutes(session.getSlotDuration());
            }
        }

        return availabilityList;
    }






    @Override
    public List<AppointmentResponseDTO> getAllAppointments() {
        return appointmentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }






    @Override
    @Transactional
    public void deleteAppointment(Long id) {
        if (!appointmentRepository.existsById(id)) {
            throw new RuntimeException("Appointment not found with ID: " + id);
        }
        appointmentRepository.deleteById(id);
    }





    public DashboardStatsDTO getAdminDashboardStats() {
        return DashboardStatsDTO.builder()
                .totalAppointments(appointmentRepository.count())
                .bookedCount(appointmentRepository.countByStatus(AppointmentStatus.BOOKED))
                .completedCount(appointmentRepository.countByStatus(AppointmentStatus.COMPLETED))
                .cancelledCount(appointmentRepository.countByStatus(AppointmentStatus.CANCELLED))
                .build();
    }




    @Override
    @Transactional
    public void setDoctorSchedule(Long doctorId, List<DoctorScheduleRequestDTO> scheduleRequests) {
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow(() -> new RuntimeException("Doctor not found with ID: "+doctorId));

        List<DoctorSchedule> oldSchedules = scheduleRepository.findByDoctorId(doctor.getId());

        if (!oldSchedules.isEmpty()) {
            scheduleRepository.deleteAllInBatch(oldSchedules);
        }
        scheduleRepository.flush();

        List<DoctorSchedule> schedules = scheduleRequests.stream().map(req -> {
            if (req.getStartTime().isAfter(req.getEndTime())) {
                throw new RuntimeException("Invalid Time Range for " + req.getDayOfWeek());
            }
            return DoctorSchedule.builder()
                    .doctor(doctor)
                    .dayOfWeek(req.getDayOfWeek().toUpperCase())
                    .startTime(req.getStartTime())
                    .endTime(req.getEndTime())
                    .slotDuration(req.getSlotDuration())
                    .maxPatients(req.getMaxPatients())
                    .isActive(true)
                    .build();
        }).collect(Collectors.toList());

        scheduleRepository.saveAll(schedules);
    }




    @Override
    @Transactional
    public void setMySchedule(String username, List<DoctorScheduleRequestDTO> scheduleRequests) {

        Doctor doctor = doctorRepository.findByUserEmail(username)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        List<DoctorSchedule> oldSchedules = scheduleRepository.findByDoctorId(doctor.getId());

        if (!oldSchedules.isEmpty()) {
            scheduleRepository.deleteAllInBatch(oldSchedules);
        }
        scheduleRepository.flush();

        List<DoctorSchedule> schedules = scheduleRequests.stream().map(req -> {
            if (req.getStartTime().isAfter(req.getEndTime())) {
                throw new RuntimeException("Invalid Time Range for " + req.getDayOfWeek());
            }
            return DoctorSchedule.builder()
                    .doctor(doctor)
                    .dayOfWeek(req.getDayOfWeek().toUpperCase())
                    .startTime(req.getStartTime())
                    .endTime(req.getEndTime())
                    .slotDuration(req.getSlotDuration())
                    .maxPatients(req.getMaxPatients())
                    .isActive(true)
                    .build();
        }).collect(Collectors.toList());

        scheduleRepository.saveAll(schedules);
    }







    @Override
    public List<DoctorScheduleRequestDTO> getDoctorScheduleById(Long doctorId) {
        List<DoctorSchedule> schedules = doctorScheduleRepository.findByDoctorId(doctorId);
        return schedules.stream()
                .map(this::convertToScheduleDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DoctorScheduleRequestDTO> getMySchedule(String email) {
        Doctor doctor = doctorRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        List<DoctorSchedule> schedules = doctorScheduleRepository.findByDoctorId(doctor.getId());

        return schedules.stream()
                .map(this::convertToScheduleDTO)
                .collect(Collectors.toList());
    }


    private DoctorScheduleRequestDTO convertToScheduleDTO(DoctorSchedule schedule) {
        DoctorScheduleRequestDTO dto = new DoctorScheduleRequestDTO();
        dto.setDayOfWeek(schedule.getDayOfWeek());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setSlotDuration(schedule.getSlotDuration());
        dto.setMaxPatients(schedule.getMaxPatients());
        dto.setActive(schedule.getIsActive());
        return dto;
    }



}