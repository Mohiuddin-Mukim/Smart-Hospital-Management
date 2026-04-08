package com.tmukimi.hospital_management.dtos;

import com.tmukimi.hospital_management.enums.UserRole;
import lombok.*;

import java.time.LocalDateTime;

 /**
    api এর কাছে ইউজারের ডাটা চাইলে সর্বোচ্চ এই কয়েকটা জিনিসই চাইতে পারে।
    তো আমার পুরো Entity পরিচয় করানোর দরকার নেই যেজন্য দরকারি জিনিসগুলোর জন্য
    আলাদা একটা ফর্মের মতো এই UserResponseDTO.
  --------------------------------------------- Mohiuddin Rahman Mukim -------------------------------------------------
 **/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDTO {
    private Long id;
    private String email;
    private UserRole role;
    private boolean emailVerified;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
