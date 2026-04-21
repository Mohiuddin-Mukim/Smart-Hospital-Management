package com.tmukimi.hospital_management.repositories;

import com.tmukimi.hospital_management.entities.Appointment;
import com.tmukimi.hospital_management.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface AppointmentRepository extends JpaRepository<Appointment,Long> {

    //to check is that the doctor busy at that time?
    boolean existsByDoctorIdAndDateAndTimeAndStatusNot(
            Long doctorId, LocalDate date, LocalTime time, AppointmentStatus status
    );


    List<Appointment> findByPatientUserEmail(String email);

    @Query("SELECT a FROM Appointment a " +
            "WHERE a.patient.user.email = :email " +
            "ORDER BY a.date DESC, a.time DESC")
    List<Appointment> findAllByPatientEmail(@Param("email") String email);



    List<Appointment> findByDoctorIdAndDate(Long doctorId, LocalDate date);


    long countByStatus(AppointmentStatus status);



    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient p WHERE a.id = :id")
    Optional<Appointment> findByIdWithPatient(@Param("id") Long id);






    @Query("SELECT COUNT(a) FROM Appointment a " +
            "LEFT JOIN Payment p ON p.appointment.id = a.id " +
            "WHERE a.doctor.id = :doctorId " +
            "AND a.date = :date " +
            "AND a.time >= :startTime " +
            "AND a.time < :endTime " +
            "AND ( " +
            "    a.status = com.tmukimi.hospital_management.enums.AppointmentStatus.BOOKED " +
            "    OR (a.status = com.tmukimi.hospital_management.enums.AppointmentStatus.PENDING " +
            "        AND (p.status = 'SUCCESS' OR (p.status = 'INITIATED' AND p.createdAt > :timeoutLimit))) " +
            ")")
    long countByDoctorIdAndDateAndStartTimeAndEndTime(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("timeoutLimit") java.time.LocalDateTime timeoutLimit);



    long countByDate(LocalDate date);




    @Query("SELECT a FROM Appointment a " +
            "WHERE a.doctor.user.email = :email " +
            "AND a.status = com.tmukimi.hospital_management.enums.AppointmentStatus.PENDING " +
            "AND a.date >= CURRENT_DATE " +
            "ORDER BY a.date ASC, a.time ASC")
    List<Appointment> findPendingRequestsByDoctorEmail(@Param("email") String email);




    @Modifying
    @Transactional
    @Query("DELETE FROM Appointment a WHERE a.doctor.id = :doctorId " +
            "AND a.date = :date AND a.time = :time " +
            "AND a.status = com.tmukimi.hospital_management.enums.AppointmentStatus.PENDING " +
            "AND EXISTS (SELECT p FROM Payment p WHERE p.appointment.id = a.id " +
            "AND p.status = 'INITIATED' AND p.createdAt < :limit)")
    void deleteAbandoned(@Param("doctorId") Long doctorId,
                         @Param("date") LocalDate date,
                         @Param("time") LocalTime time,
                         @Param("limit") LocalDateTime limit);




    @Query(value = "SELECT CAST(date AS DATE) as appointment_date, COUNT(*) as total_count " +
            "FROM appointments " +
            "WHERE date >= :startDate " +
            "GROUP BY CAST(date AS DATE) " +
            "ORDER BY CAST(date AS DATE) ASC", nativeQuery = true)
    List<Object[]> getAppointmentCountLast7Days(@Param("startDate") LocalDate startDate);




    // সব সফল অ্যাপয়েন্টমেন্ট থেকে মোট আয় (যদি status PAID/COMPLETED থাকে তবে Filter করতে পারেন)
    @Query("SELECT SUM(a.fee) FROM Appointment a")
    BigDecimal calculateTotalRevenue();

    // আজকের মোট আয়
    @Query("SELECT SUM(a.fee) FROM Appointment a WHERE a.date = CURRENT_DATE")
    BigDecimal calculateTodayRevenue();



//    @Query("""
//    SELECT a.time FROM Appointment a
//    WHERE a.doctor.id = :doctorId
//      AND a.date = :date
//      AND a.time >= :startTime
//      AND a.time <= :endTime
//      AND (a.status = 'BOOKED' OR a.createdAt >= :timeout)
//""")
//    List<LocalTime> findBookedStartTimes(
//            @Param("doctorId") Long doctorId,
//            @Param("date") LocalDate date,
//            @Param("startTime") LocalTime startTime,
//            @Param("endTime") LocalTime endTime,
//            @Param("timeout") LocalDateTime timeout
//    );

    @Query("""
    SELECT a.time FROM Appointment a
    WHERE a.doctor.id = :doctorId
      AND a.date = :date
      AND a.time >= :startTime
      AND a.time <= :endTime
      AND (
          a.status IN (com.tmukimi.hospital_management.enums.AppointmentStatus.BOOKED, 
                       com.tmukimi.hospital_management.enums.AppointmentStatus.COMPLETED)
          OR 
          (a.status = com.tmukimi.hospital_management.enums.AppointmentStatus.PENDING 
           AND a.createdAt >= :timeout)
      )
""")
    List<LocalTime> findBookedStartTimes(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("timeout") LocalDateTime timeout
    );



    // AppointmentRepository.java
    List<Appointment> findAllByStatusOrderByIdDesc(AppointmentStatus status);

}