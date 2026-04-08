package com.tmukimi.hospital_management.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QueueStatusDTO {
    private int mySerial;
    private int currentlyServing;
    private String doctorName;
    private String status;
}
