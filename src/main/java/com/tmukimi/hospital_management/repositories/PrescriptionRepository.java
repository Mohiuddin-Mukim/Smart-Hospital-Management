
package com.tmukimi.hospital_management.repositories;

import com.tmukimi.hospital_management.entities.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    // PrescriptionRepository.java
    @Query("SELECT p.appointment.doctor.name as doctorName, COUNT(p) as totalPrescriptions " +
            "FROM Prescription p GROUP BY p.appointment.doctor.id")
    List<Object[]> getDoctorPerformanceData();


    Optional<Prescription> findByAppointmentId(Long appointmentId);
    Optional<Prescription> findByVerificationToken(String token);




//    @Query(value = "SELECT pm.medicine_id, COUNT(pm.medicine_id) as count " +
//            "FROM prescription_medicines_aud pm " +
//            "GROUP BY pm.medicine_id " +
//            "ORDER BY count DESC LIMIT 5", nativeQuery = true)
//    List<Object[]> getTopMedicinesFromAudit();

    @Query(value = "SELECT m.generic_name as name, COUNT(pm.medicine_id) as count " +
            "FROM prescription_medicines_aud pm " +
            "JOIN medicines m ON pm.medicine_id = m.id " +
            "GROUP BY pm.medicine_id, m.generic_name " +
            "ORDER BY count DESC LIMIT 5", nativeQuery = true)
    List<Object[]> getTopMedicinesFromAudit();



    @Query(value = "SELECT diagnosis, COUNT(diagnosis) as count " +
            "FROM prescriptions_aud " +
            "WHERE diagnosis IS NOT NULL " +
            "GROUP BY diagnosis " +
            "ORDER BY count DESC LIMIT 5", nativeQuery = true)
    List<Object[]> getTopDiagnosisFromAudit();

}