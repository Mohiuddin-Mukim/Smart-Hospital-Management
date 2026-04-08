package com.tmukimi.hospital_management.mappers;

import com.tmukimi.hospital_management.dtos.UserRequestDTO;
import com.tmukimi.hospital_management.dtos.UserResponseDTO;
import com.tmukimi.hospital_management.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

 /**
    UserMapper converts between DTOs and the User entity.
    What exactly does UserMapper do?
     1. When user sends data:(like log in time এ) (RequestDTO -> Entity)
         UserRequestDTO
          {
           email,
           password,
           role
           }
        Mapper converts it into:
        User{
          email,
          hashedPassword,
          role,
          active = true,
          emailVerified = false,
          createdAt = now()
          }
     2. একইভাবে উল্টোটাও, When backend responds: (Entity -> ResponseDTO)
        User{
         id,
         email,
         role,
         active,
         createdAt,
         updatedAt, // পাসওয়ার্ডটা বাদ থাকবে কারণ পাসওয়ার্ড এর দরকার নেইতো এক্সপোজ করার।
         }

   --------------------------------------------- Mohiuddin Rahman Mukim ------------------------------------------------
 **/


@Component
@RequiredArgsConstructor
public class UserMapper {
    private final PasswordEncoder passwordEncoder;

    public User toEntity(UserRequestDTO dto){
        return User.builder()
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .active(true)
                .emailVerified(false)
                .build();
    }




    public UserResponseDTO toDTO(User user){
        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .emailVerified(user.isEmailVerified())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

}