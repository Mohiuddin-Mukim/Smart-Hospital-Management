package com.tmukimi.hospital_management.repositories;

import com.tmukimi.hospital_management.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByAppointmentId(Long appointmentId);

    Optional<Payment> findByTransactionId(String transactionId);


    List<Payment> findByStatusAndCreatedAtBefore(String status, LocalDateTime dateTime);


    @Modifying
    @Transactional
    @Query("DELETE FROM Payment p WHERE p.status = :status AND p.createdAt < :cutoff")
    void deleteAbandonedPayments(@Param("status") String status, @Param("cutoff") LocalDateTime cutoff);
}