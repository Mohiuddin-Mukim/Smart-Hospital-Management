package com.tmukimi.hospital_management.repositories;

import com.tmukimi.hospital_management.entities.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    // শুধুমাত্র সেই নোটিশগুলো নিবে যেগুলো isActive = true
    List<Notice> findByIsActiveTrue();
    Optional<Notice> findById(Long id);
}