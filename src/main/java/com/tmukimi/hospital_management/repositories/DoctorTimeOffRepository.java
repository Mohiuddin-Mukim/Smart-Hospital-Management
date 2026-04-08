package com.tmukimi.hospital_management.repositories;

import com.tmukimi.hospital_management.entities.DoctorTimeOff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface DoctorTimeOffRepository extends JpaRepository<DoctorTimeOff, Long> {

    // চেক করবে ওইদিন ডাক্তার ছুটিতে আছে কী না
    boolean existsByDoctorIdAndOffDate(Long doctorId, LocalDate offDate);
}
