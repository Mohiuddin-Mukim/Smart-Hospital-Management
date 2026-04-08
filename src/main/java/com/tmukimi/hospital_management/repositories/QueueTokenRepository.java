package com.tmukimi.hospital_management.repositories;

import com.tmukimi.hospital_management.entities.QueueToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QueueTokenRepository extends JpaRepository<QueueToken, Long> {

    Optional<QueueToken> findByAppointmentId(Long appointmentId);
}