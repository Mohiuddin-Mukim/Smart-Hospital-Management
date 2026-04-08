package com.tmukimi.hospital_management.repositories;

import com.tmukimi.hospital_management.entities.DoctorProfileRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorProfileRequestRepository extends JpaRepository<DoctorProfileRequest, Long> {
    List<DoctorProfileRequest> findByProcessedFalse();
}