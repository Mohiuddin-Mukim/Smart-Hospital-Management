package com.tmukimi.hospital_management.repositories;

import com.tmukimi.hospital_management.entities.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByUserId(Long userId);
    Optional<Doctor> findByUserEmail(String email);
    List<Doctor> findByProfileVerifiedFalse();

    @Query("SELECT d FROM Doctor d WHERE d.user.active = true")
    List<Doctor> findAllActiveDoctors();


    @Query("SELECT COUNT(d) FROM Doctor d WHERE d.user.active = true")
    long countActiveDoctors();

}
