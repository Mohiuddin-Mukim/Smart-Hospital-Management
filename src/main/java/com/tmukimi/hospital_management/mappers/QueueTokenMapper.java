package com.tmukimi.hospital_management.mappers;

import com.tmukimi.hospital_management.dtos.QueueTokenResponseDTO;
import com.tmukimi.hospital_management.entities.QueueToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class QueueTokenMapper {

    public QueueTokenResponseDTO toDTO(QueueToken entity){
        if(entity == null){
            return null;
        }

        LocalDate appointmentDate = entity.getAppointment().getDate();
        boolean expired = appointmentDate.isBefore(LocalDate.now());

        String message = expired ? "এই অ্যাপয়েন্টমেন্টের তারিখ পার হয়ে গেছে।" : "আপনার টোকেনটি বর্তমানে সক্রিয় আছে।";
        return new  QueueTokenResponseDTO(
                entity.getSerialNo(),
                entity.getStatus(),
                expired,
                message
        );
    }
}
