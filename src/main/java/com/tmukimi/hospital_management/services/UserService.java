package com.tmukimi.hospital_management.services;

import com.tmukimi.hospital_management.dtos.UserRequestDTO;
import com.tmukimi.hospital_management.dtos.UserResponseDTO;
import com.tmukimi.hospital_management.entities.User;
import com.tmukimi.hospital_management.enums.UserRole;
import com.tmukimi.hospital_management.exceptions.DuplicateEmailException;
import com.tmukimi.hospital_management.exceptions.ResourceNotFoundException;
import com.tmukimi.hospital_management.mappers.UserMapper;
import com.tmukimi.hospital_management.repositories.DoctorRepository;
import com.tmukimi.hospital_management.repositories.PatientRepository;
import com.tmukimi.hospital_management.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public UserResponseDTO createUser(UserRequestDTO dto){

        // Check if email is already exists
        if(userRepository.existsByEmail(dto.getEmail())){
            throw new DuplicateEmailException("Email already exists: " + dto.getEmail());
        }

        User user = userMapper.toEntity(dto);      // Frontend থেকে UserRequestDTO এসেছে, Mapper দিয়ে database-ready User entity বানাচ্ছি
        User saved = userRepository.save(user);    // এখানে আমরা User entity database এ সেইভ করতেছি, তাই saved object এ সব ফাইনাল ডাটাবেজ ইনফো থাকবে
        return userMapper.toDTO(saved);            // আমরা ডাটাবেজের অবজেক্ট ফ্রন্টএন্ডে পাঠাচ্ছি, এজন্য Mapper দিয়ে DTO রেডি অবজেক্ট বানাচ্ছি।
        // পুরো ম্যাথডের ফ্লো কিছুটা এমন,
        // UserRequestDTO --> convert into Entity using Mapper and send to database -->
        // -> saved into DB using repository --> return Entity object from DB to frontend and convert into DTO using mapper.
        // বা, Frontend → UserRequestDTO → Mapper → User → Save to DB → Mapper → UserResponseDTO → Frontend
    }


    public List<UserResponseDTO> getAllActiveUsers(){
        return userRepository.findAllByActiveTrue()
                .stream()
                .map(userMapper::toDTO)   //“প্রতিটা User entity কে → UserResponseDTO তে convert করে”
                .collect(Collectors.toList());   // কনভার্টেড আউটপুটগুলোর আবার একটা লিস্ট
        // Flow:  Database → active users আনো একে একে loop করো → প্রত্যেকটা user → DTO বানাও → সব DTO একসাথে list করো → সেই list return করো
    }


    public UserResponseDTO getUserById(Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found!"));
        return userMapper.toDTO(user);
    }

    public void deactivateUser(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        user.deactivate();
        userRepository.save(user);
    }




    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == UserRole.ADMIN) {
            throw new RuntimeException("Admins cannot be deleted!");
        }

        if (user.getRole() == UserRole.DOCTOR) {
            doctorRepository.findByUserId(userId).ifPresent(doctorRepository::delete);
        } else if (user.getRole() == UserRole.PATIENT) {
            patientRepository.findByUserId(userId).ifPresent(patientRepository::delete);
        }

        userRepository.delete(user);
    }
}
