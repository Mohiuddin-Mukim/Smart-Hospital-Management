package com.tmukimi.hospital_management.services;

import com.tmukimi.hospital_management.entities.Appointment;
import com.tmukimi.hospital_management.entities.Payment;
import com.tmukimi.hospital_management.repositories.AppointmentRepository;
import com.tmukimi.hospital_management.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentCleanupService {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void cleanupExpiredAppointments() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(1);

        List<Payment> expiredPayments = paymentRepository.findByStatusAndCreatedAtBefore("INITIATED", threshold);

        if (!expiredPayments.isEmpty()) {
            for (Payment payment : expiredPayments) {
                Appointment appt = payment.getAppointment();

                paymentRepository.delete(payment);
                if (appt != null) {
                    appointmentRepository.delete(appt);
                }
            }
            System.out.println("Scheduler: Deleted " + expiredPayments.size() + " expired booking attempts.");
        }
    }
}