package com.tmukimi.hospital_management.services;

import com.tmukimi.hospital_management.dtos.PrescriptionRequestDTO;
import com.tmukimi.hospital_management.entities.*;
import com.tmukimi.hospital_management.enums.QueueStatus;
import com.tmukimi.hospital_management.repositories.*;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionMedicineRepository pmRepository;
    private final QueueTokenRepository queueRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineBrandRepository brandRepository;
    private final jakarta.persistence.EntityManager entityManager;

    @Transactional
    public void savePrescription(PrescriptionRequestDTO dto) {

        Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found with ID: " + dto.getAppointmentId()));

        Prescription prescription = new Prescription();
        prescription.setAppointment(appointment);

        prescription.setWeight(dto.getWeight());
        prescription.setBloodPressure(dto.getBp());
        prescription.setTemperature(dto.getTemperature());
        prescription.setPulse(dto.getPulse());

        prescription.setChiefComplaints(dto.getChiefComplaints());
        prescription.setClinicalFindings(dto.getClinicalFindings());
        prescription.setDiagnosis(dto.getDiagnosis());
        prescription.setAdvice(dto.getAdvice());
        prescription.setNotes(dto.getNotes());
        prescription.setNextVisitDate(dto.getNextVisitDate());

        Prescription savedPrescription = prescriptionRepository.save(prescription);

        if (dto.getMedicines() != null && !dto.getMedicines().isEmpty()) {
            List<PrescriptionMedicine> meds = dto.getMedicines().stream().map(m -> {
                PrescriptionMedicine pm = new PrescriptionMedicine();
                pm.setPrescription(savedPrescription);

                Medicine medicine = medicineRepository.findById(m.getMedicineId())
                        .orElseThrow(() -> new RuntimeException("Generic Medicine not found ID: " + m.getMedicineId()));
                MedicineBrand brand = brandRepository.findById(m.getBrandId())
                        .orElseThrow(() -> new RuntimeException("Brand not found ID: " + m.getBrandId()));

                pm.setMedicine(medicine);
                pm.setBrand(brand);
                pm.setDosage(m.getDosage());
                pm.setDuration(m.getDuration());
                pm.setInstruction(m.getInstruction());
                pm.setDays(m.getDays() != null ? m.getDays() : 0);
                pm.setContinued(m.isContinued());

                return pm;
            }).collect(Collectors.toList());

            pmRepository.saveAll(meds);
        }

        queueRepository.findByAppointmentId(dto.getAppointmentId()).ifPresent(token -> {
            token.setStatus(QueueStatus.DONE);
            queueRepository.save(token);
        });

        System.out.println(">>> Prescription saved successfully for Appointment ID: " + dto.getAppointmentId());
    }





    public List<PrescriptionMedicine> getPatientMedicineHistory(Long patientId) {
        List<PrescriptionMedicine> history = pmRepository.findAllByPatientId(patientId);
        if (history.isEmpty()) {
            throw new RuntimeException("No medicine history found for Patient ID: " + patientId);
        }
        return history;
    }




    @Transactional(readOnly = true)
    public List<?> getMedicineAuditTrail(Long medicineId) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        return auditReader.createQuery()
                .forRevisionsOfEntity(PrescriptionMedicine.class, true, true)
                .add(AuditEntity.id().eq(medicineId))
                .getResultList();
    }
}