package com.tmukimi.hospital_management.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDTO {
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String email;
    private String role;
}
