package com.tmukimi.hospital_management.repositories;

import com.tmukimi.hospital_management.entities.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {


    Optional<DoctorSchedule> findByDoctorIdAndDayOfWeekAndIsActiveTrue(Long doctorId, String dayOfWeek);

    List<DoctorSchedule> findByDoctorId(Long doctorId);

    List<DoctorSchedule> findAllByDoctorIdAndDayOfWeekAndIsActiveTrue(Long doctorId, String dayOfWeek);

    @Modifying
    @Transactional
    @Query("DELETE FROM DoctorSchedule ds WHERE ds.doctor.id = :doctorId")
    void deleteByDoctorId(@Param("doctorId") Long doctorId);
}
