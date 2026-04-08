package com.tmukimi.hospital_management.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AuditLogResponseDTO {
    private Object entity;
    private int revisionId;       // রিভিশন আইডি
    private String revisionType;  // ADD, MOD, or DEL
    private LocalDateTime timestamp;
    private String modifiedBy;
}