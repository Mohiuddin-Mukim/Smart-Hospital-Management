package com.tmukimi.hospital_management.dtos;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class PrescriptionRequestDTO {
    private Long appointmentId;

    private String weight;
    private String bp;
    private String temperature;
    private String pulse;

    private String diagnosis;
    private String chiefComplaints;
    private String clinicalFindings;
    private String advice;
    private String notes;
    private LocalDate nextVisitDate;

    private List<MedicineItemDTO> medicines;

    @Data
    public static class MedicineItemDTO {
        private Long brandId;
        private Long medicineId;
        private String dosage;
        private String duration;
        private String instruction;
        private Integer days;
        private boolean isContinued;
    }
}