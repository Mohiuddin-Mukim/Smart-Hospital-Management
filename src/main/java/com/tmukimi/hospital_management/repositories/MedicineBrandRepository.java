package com.tmukimi.hospital_management.repositories;

import com.tmukimi.hospital_management.entities.MedicineBrand;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MedicineBrandRepository extends JpaRepository<MedicineBrand, Long> {

    @Query("SELECT b FROM MedicineBrand b JOIN FETCH b.medicine WHERE b.brandName LIKE :query%")
    List<MedicineBrand> searchByBrandName(@Param("query") String query, Pageable pageable);
}