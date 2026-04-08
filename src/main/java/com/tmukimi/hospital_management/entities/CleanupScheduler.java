package com.tmukimi.hospital_management.entities;

import com.tmukimi.hospital_management.repositories.AppointmentRepository;
import com.tmukimi.hospital_management.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CleanupScheduler {
    private final AppointmentRepository appointmentRepository;
    private final PaymentRepository paymentRepository;

    @Scheduled(fixedRate = 100000) ///it will run in every one minute{Mohiuddin Rahman Mukim}
    @Transactional
    public void cleanupAbandonedAppointments() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(15);

        List<Payment> abandoned = paymentRepository.findByStatusAndCreatedAtBefore("INITIATED", cutoff);

        if (!abandoned.isEmpty()) {
            for (Payment p : abandoned) {
                Long appId = p.getAppointment().getId();
                paymentRepository.delete(p);
                appointmentRepository.deleteById(appId);
            }
            System.out.println(abandoned.size() + " abandoned slots cleared.");
        }
    }
}