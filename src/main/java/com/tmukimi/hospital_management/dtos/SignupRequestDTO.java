package com.tmukimi.hospital_management.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
    DTO হচ্ছে একটা সিম্পল অবজেক্ট এর মতো যেটা শুধু ব্যবহার করা হয় ডাটা কে একটা লেয়ার থেকে আরেকটা লেয়ারে মুভ করানোর জন্য।
    কেনো??
    কারণ আমি চাইনা আমার Entity সবজায়গায় expose হয়ে যাক।
    কল্পনা করিঃ
       এখন ইউজারের ডাটা ফ্রন্টএন্ডে পাঠাতে চাই, তো ওখানেতো আর পাসওয়ার্ড পাঠাবোনা, তাইনা?
       আবার যেসব জিনিস Login এর জন্য লাগবে সেগুলো specify করে দিছি UserRequestDTO তে,
       আর যেসব যেসব জিনিস এপিআইয়ের কাছে চাবে মানে data going out সেক্ষেত্রে পাসওয়ার্ড বা যেগুলো পাঠাবোনা
         ওগুলা বাদে বাকিগুলো UserResponseDTO তে রাখবো।
 ------------------------------------------------- Mohiuddin Rahman Mukim ----------------------------------------------
**/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequestDTO {

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is mandatory")
    private String email;

    @NotBlank(message = "Password is mandatory")
    @Size(min=6, max = 255, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Age is required")
    @Min(0)
    @Max(120)
    private Integer age;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Phone number is required")
    private String phone;

    private String address;

}