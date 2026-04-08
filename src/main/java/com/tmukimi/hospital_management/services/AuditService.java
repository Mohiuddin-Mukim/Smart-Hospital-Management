package com.tmukimi.hospital_management.services;

import com.tmukimi.hospital_management.dtos.AuditLogResponseDTO;
import com.tmukimi.hospital_management.entities.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final EntityManager entityManager;

    private <T> List<AuditLogResponseDTO> getPaginatedEntityHistory(Class<T> entityClass, Long id, int page, int size) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        List<Object[]> results = auditReader.createQuery()
                .forRevisionsOfEntity(entityClass, false, true)
                .add(AuditEntity.id().eq(id))
                .addOrder(AuditEntity.revisionNumber().desc())
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();

        return results.stream().map(result -> {
            T entity = (T) result[0];
            CustomRevisionEntity revEntity = (CustomRevisionEntity) result[1];
            RevisionType revType = (RevisionType) result[2];

            return new AuditLogResponseDTO(
                    entity,
                    revEntity.getId(),
                    revType.name(),
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(revEntity.getTimestamp()), ZoneId.systemDefault()),
                    revEntity.getModifiedBy()
            );
        }).collect(Collectors.toList());
    }

    public List<AuditLogResponseDTO> getUserRevisionHistory(Long userId, int page, int size) {
        return getPaginatedEntityHistory(User.class, userId, page, size);
    }

    public List<AuditLogResponseDTO> getDoctorRevisionHistory(Long doctorId, int page, int size) {
        return getPaginatedEntityHistory(Doctor.class, doctorId, page, size);
    }

    public List<AuditLogResponseDTO> getPatientRevisionHistory(Long patientId, int page, int size) {
        return getPaginatedEntityHistory(Patient.class, patientId, page, size);
    }

    public List<AuditLogResponseDTO> getAppointmentRevisionHistory(Long appointmentId, int page, int size) {
        return getPaginatedEntityHistory(Appointment.class, appointmentId, page, size);
    }



    public List<AuditLogResponseDTO> getGlobalAuditLogs(int page, int size) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        List<AuditLogResponseDTO> allLogs = new ArrayList<>();

        List<Class<?>> auditedClasses = List.of(User.class, Doctor.class, Appointment.class, Patient.class);

        for (Class<?> clazz : auditedClasses) {
            try {
                List<Object[]> results = auditReader.createQuery()
                        .forRevisionsOfEntity(clazz, false, true)
                        .addOrder(AuditEntity.revisionNumber().desc())
                        .setMaxResults(size * (page + 1))
                        .getResultList();

                for (Object[] result : results) {
                    CustomRevisionEntity revEntity = (CustomRevisionEntity) result[1];
                    RevisionType revType = (RevisionType) result[2];

                    String displayInfo = clazz.getSimpleName() + ": ";

                    if (result[0] instanceof User u) displayInfo += u.getEmail();
                    else if (result[0] instanceof Doctor d) displayInfo += d.getName();
                    else if (result[0] instanceof Appointment a) displayInfo += "Appmnt #" + a.getId();
                    else displayInfo += "ID " + result[0].toString();

                    allLogs.add(new AuditLogResponseDTO(
                            displayInfo,
                            revEntity.getId(),
                            revType.name(),
                            LocalDateTime.ofInstant(Instant.ofEpochMilli(revEntity.getTimestamp()), ZoneId.systemDefault()),
                            revEntity.getModifiedBy()
                    ));
                }
            } catch (Exception e) {
                System.err.println("Skipping audit for " + clazz.getSimpleName());
            }
        }

        return allLogs.stream()
                .sorted(Comparator.comparing(AuditLogResponseDTO::getTimestamp).reversed())
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
    }

}