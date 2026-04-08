package com.tmukimi.hospital_management.repositories;

import com.tmukimi.hospital_management.entities.Doctor;
import com.tmukimi.hospital_management.entities.QueueTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface QueueTrackerRepository extends JpaRepository<QueueTracker, Long> {

    Optional<QueueTracker> findByDoctorAndDate(Doctor doctor, LocalDate date);
}