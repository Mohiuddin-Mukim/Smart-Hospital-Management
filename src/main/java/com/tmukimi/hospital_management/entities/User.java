package com.tmukimi.hospital_management.entities;

import com.tmukimi.hospital_management.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

@Entity
@Table(name = "users",
        uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Audited
@SQLDelete(sql = "UPDATE users SET is_active = false, email = CONCAT(email, '_deleted_', UNIX_TIMESTAMP()) WHERE id = ?")
@SQLRestriction("is_active = true") // ডিফল্টভাবে শুধু অ্যাক্টিভ ইউজার আনবে
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 150)
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is mandatory")
    @Size(max = 150)
    private String email;

    @NotAudited
    @NotBlank(message = "Password is mandatory")
    @Size(min = 6, max = 255)
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;


    // এটা একটা হেল্পার মেথড। Instead of removing the user from the database, it just marks them as inactive
    public void deactivate() {
        this.active = false;
    }


}