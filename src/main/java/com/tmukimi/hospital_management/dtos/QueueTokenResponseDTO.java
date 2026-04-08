package com.tmukimi.hospital_management.dtos;

import com.tmukimi.hospital_management.enums.QueueStatus;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueueTokenResponseDTO {
    private int serialNumber;
    private QueueStatus status;
    private boolean isExpired;
    private String message;
}
