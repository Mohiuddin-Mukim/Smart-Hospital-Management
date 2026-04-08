package com.tmukimi.hospital_management.repositories;

import com.tmukimi.hospital_management.entities.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    Optional<Medicine> findByGenericName(String genericName);
}