package com.tmukimi.hospital_management.repositories;

import com.tmukimi.hospital_management.entities.User;
import com.tmukimi.hospital_management.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> , RevisionRepository<User, Long, Integer> {   // to support Envers(for automatic audit) we extend RevisionRepository

    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);   // there might a value or not যেজন্য Optional use করেছি
    List<User> findByRole(UserRole role);
    List<User> findAllByActiveTrue();
}
