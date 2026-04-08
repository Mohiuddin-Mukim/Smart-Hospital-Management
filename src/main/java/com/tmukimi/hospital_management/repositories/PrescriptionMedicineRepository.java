package com.tmukimi.hospital_management.repositories;

import com.tmukimi.hospital_management.entities.PrescriptionMedicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionMedicineRepository extends JpaRepository<PrescriptionMedicine, Long> {
    List<PrescriptionMedicine> findByPrescriptionId(Long prescriptionId);

    @Query("SELECT pm FROM PrescriptionMedicine pm JOIN pm.prescription p WHERE p.appointment.patient.id = :patientId ORDER BY p.createdAt DESC")
    List<PrescriptionMedicine> findAllByPatientId(@Param("patientId") Long patientId);
}