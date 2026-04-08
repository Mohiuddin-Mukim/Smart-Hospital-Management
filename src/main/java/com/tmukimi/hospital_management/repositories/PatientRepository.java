package com.tmukimi.hospital_management.repositories;

import com.tmukimi.hospital_management.entities.Patient;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByName(String name);
    Optional<Patient> findByUserId(Long userId);
    Optional<Patient> findByUserEmail(String email);

    @EntityGraph(attributePaths = {"user"}) //entity তে যে ভ্যারিয়েবল নাম দিছি
    List<Patient> findAll();
}