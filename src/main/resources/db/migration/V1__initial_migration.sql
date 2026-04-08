-- ============================
-- CREATE SCHEMA
-- ============================
-- Note: Flyway সাধারণত schema তৈরি করে দেয়,
CREATE DATABASE IF NOT EXISTS management;
USE management;

-- ============================
-- USERS TABLE (AUTH CORE)
-- ============================
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,

                       email VARCHAR(150) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,

                       role VARCHAR(20) NOT NULL,
                       email_verified TINYINT(1) NOT NULL DEFAULT 0,
                       is_active TINYINT(1) NOT NULL DEFAULT 1,

                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                           ON UPDATE CURRENT_TIMESTAMP,

                       CHECK (role IN ('ADMIN', 'DOCTOR', 'PATIENT'))
) ENGINE=InnoDB;

-- ============================
-- REFRESH TOKENS (JWT SUPPORT)
-- ============================
CREATE TABLE refresh_tokens (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                user_id BIGINT NOT NULL,
                                token VARCHAR(255) NOT NULL UNIQUE,

                                expiry_date TIMESTAMP NOT NULL,
                                revoked TINYINT(1) NOT NULL DEFAULT 0,

                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT fk_refresh_tokens_user
                                    FOREIGN KEY (user_id) REFERENCES users(id)
                                        ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================
-- PATIENTS TABLE
-- ============================
CREATE TABLE patients (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,

                          user_id BIGINT NOT NULL UNIQUE,
                          name VARCHAR(150) NOT NULL,
                          age INT,
                          gender VARCHAR(10),
                          phone VARCHAR(20),
                          address TEXT,

                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                              ON UPDATE CURRENT_TIMESTAMP,

                          CONSTRAINT fk_patients_user
                              FOREIGN KEY (user_id) REFERENCES users(id)
                                  ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================
-- DOCTORS TABLE
-- ============================
CREATE TABLE doctors (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,

                         user_id BIGINT NOT NULL UNIQUE,
                         name VARCHAR(150) NOT NULL,
                         specialization VARCHAR(100),
                         phone VARCHAR(20),
                         room_no VARCHAR(20),

                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                             ON UPDATE CURRENT_TIMESTAMP,

                         CONSTRAINT fk_doctors_user
                             FOREIGN KEY (user_id) REFERENCES users(id)
                                 ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================
-- DOCTOR SCHEDULES & TIME OFF
-- ============================
CREATE TABLE doctor_schedules (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  doctor_id BIGINT NOT NULL,
                                  day_of_week VARCHAR(10) NOT NULL,
                                  start_time TIME NOT NULL,
                                  end_time TIME NOT NULL,
                                  slot_duration INT DEFAULT 15 NOT NULL,
                                  max_patients INT DEFAULT 20 NOT NULL,
                                  is_active TINYINT(1) DEFAULT 1 NOT NULL,
                                  CONSTRAINT fk_schedule_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id) ON DELETE CASCADE,
                                  CHECK (`day_of_week` IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'))
) ENGINE=InnoDB;

CREATE TABLE doctor_time_off (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 doctor_id BIGINT NOT NULL,
                                 off_date DATE NOT NULL,
                                 reason VARCHAR(255),
                                 CONSTRAINT uq_doctor_off UNIQUE (doctor_id, off_date),
                                 CONSTRAINT fk_timeoff_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================
-- APPOINTMENTS
-- ============================
CREATE TABLE appointments (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,

                              patient_id BIGINT NOT NULL,
                              doctor_id BIGINT NOT NULL,

                              `date` DATE NOT NULL,
                              `time` TIME NOT NULL,

                              status VARCHAR(20) NOT NULL DEFAULT 'BOOKED',
                              reason TEXT,

                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP
                                  ON UPDATE CURRENT_TIMESTAMP,

                              CHECK (status IN ('PENDING', 'BOOKED', 'COMPLETED', 'CANCELLED', 'REJECTED', 'NO_SHOW')),

                              CONSTRAINT fk_appointments_patient
                                  FOREIGN KEY (patient_id) REFERENCES patients(id)
                                      ON DELETE CASCADE,

                              CONSTRAINT fk_appointments_doctor
                                  FOREIGN KEY (doctor_id) REFERENCES doctors(id)
                                      ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================
-- PRESCRIPTIONS
-- ============================
CREATE TABLE prescriptions (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,

                               appointment_id BIGINT NOT NULL UNIQUE,
                               diagnosis TEXT,
                               notes TEXT,

                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_prescriptions_appointment
                                   FOREIGN KEY (appointment_id) REFERENCES appointments(id)
                                       ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================
-- MEDICINES
-- ============================
CREATE TABLE medicines (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           generic_name VARCHAR(100) NOT NULL,
                           description TEXT,
                           side_effects TEXT
) ENGINE=InnoDB;

-- ============================
-- MEDICINE BRANDS
-- ============================
CREATE TABLE medicine_brands (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                 medicine_id BIGINT NOT NULL,
                                 brand_name VARCHAR(100) NOT NULL,
                                 company_name VARCHAR(100),

                                 CONSTRAINT fk_brands_medicine
                                     FOREIGN KEY (medicine_id) REFERENCES medicines(id)
                                         ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================
-- PRESCRIPTION MEDICINES
-- ============================
CREATE TABLE prescription_medicines (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                        prescription_id BIGINT NOT NULL,
                                        medicine_id BIGINT NOT NULL,
                                        brand_id BIGINT,

                                        dosage VARCHAR(50),
                                        duration VARCHAR(50),

                                        CONSTRAINT fk_pm_prescription
                                            FOREIGN KEY (prescription_id) REFERENCES prescriptions(id)
                                                ON DELETE CASCADE,

                                        CONSTRAINT fk_pm_medicine
                                            FOREIGN KEY (medicine_id) REFERENCES medicines(id),

                                        CONSTRAINT fk_pm_brand
                                            FOREIGN KEY (brand_id) REFERENCES medicine_brands(id)
) ENGINE=InnoDB;

-- ============================
-- BILLS
-- ============================
CREATE TABLE bills (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,

                       patient_id BIGINT NOT NULL,
                       appointment_id BIGINT UNIQUE,

                       amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                       status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',

                       `date` DATE NOT NULL DEFAULT (CURRENT_DATE),

                       CHECK (status IN ('PAID', 'UNPAID')),

                       CONSTRAINT fk_bills_patient
                           FOREIGN KEY (patient_id) REFERENCES patients(id)
                               ON DELETE CASCADE,

                       CONSTRAINT fk_bills_appointment
                           FOREIGN KEY (appointment_id) REFERENCES appointments(id)
) ENGINE=InnoDB;

-- ============================
-- QUEUE TRACKER
-- ============================
CREATE TABLE queue_tracker (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,

                               doctor_id BIGINT NOT NULL,
                               `date` DATE NOT NULL,

                               current_serial INT NOT NULL DEFAULT 0,
                               last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                                   ON UPDATE CURRENT_TIMESTAMP,

                               CONSTRAINT uq_queue_doctor_date UNIQUE (doctor_id, `date`),

                               CONSTRAINT fk_queue_doctor
                                   FOREIGN KEY (doctor_id) REFERENCES doctors(id)
                                       ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================
-- QUEUE TOKENS
-- ============================
CREATE TABLE queue_tokens (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,

                              appointment_id BIGINT NOT NULL UNIQUE,
                              serial_no INT NOT NULL,

                              status VARCHAR(20) NOT NULL DEFAULT 'WAITING',

                              CHECK (status IN ('WAITING', 'INSIDE', 'DONE')),

                              CONSTRAINT fk_queue_token_appointment
                                  FOREIGN KEY (appointment_id) REFERENCES appointments(id)
                                      ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================
-- BLOOD DONORS
-- ============================
CREATE TABLE blood_donors (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,

                              name VARCHAR(150) NOT NULL,
                              blood_group VARCHAR(5) NOT NULL,
                              phone VARCHAR(20),
                              location VARCHAR(150),
                              last_donation_date DATE,

                              CHECK (blood_group IN ('A+','A-','B+','B-','AB+','AB-','O+','O-'))
) ENGINE=InnoDB;

-- ============================
-- BLOOD REQUESTS
-- ============================
CREATE TABLE blood_requests (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                blood_group VARCHAR(5) NOT NULL,
                                patient_name VARCHAR(150),
                                hospital VARCHAR(150),

                                status VARCHAR(20) NOT NULL,

                                CHECK (blood_group IN ('A+','A-','B+','B-','AB+','AB-','O+','O-')),
                                CHECK (status IN ('OPEN','FULFILLED'))
) ENGINE=InnoDB;

-- =====================================================
-- ===================== TRIGGERS ======================
-- =====================================================

DELIMITER $$

CREATE TRIGGER trg_create_queue_tracker
    BEFORE INSERT ON appointments
    FOR EACH ROW
BEGIN
    INSERT IGNORE INTO queue_tracker (doctor_id, `date`, current_serial)
    VALUES (NEW.doctor_id, NEW.`date`, 0);
END$$

CREATE TRIGGER trg_create_queue_token
    AFTER INSERT ON appointments
    FOR EACH ROW
BEGIN
    DECLARE next_serial INT;

    UPDATE queue_tracker
    SET current_serial = current_serial + 1
    WHERE doctor_id = NEW.doctor_id
      AND `date` = NEW.`date`;

    SELECT current_serial
    INTO next_serial
    FROM queue_tracker
    WHERE doctor_id = NEW.doctor_id
      AND `date` = NEW.`date`;

    INSERT INTO queue_tokens (appointment_id, serial_no, status)
    VALUES (NEW.id, next_serial, 'WAITING');
END$$

CREATE TRIGGER trg_complete_appointment
    AFTER UPDATE ON queue_tokens
    FOR EACH ROW
BEGIN
    IF OLD.status <> 'DONE' AND NEW.status = 'DONE' THEN
        UPDATE appointments
        SET status = 'COMPLETED'
        WHERE id = NEW.appointment_id;
    END IF;
END$$

CREATE TRIGGER trg_create_bill
    AFTER INSERT ON prescriptions
    FOR EACH ROW
BEGIN
    DECLARE p_id BIGINT;

    SELECT patient_id
    INTO p_id
    FROM appointments
    WHERE id = NEW.appointment_id;

    INSERT INTO bills (patient_id, appointment_id, amount, status, `date`)
    VALUES (p_id, NEW.appointment_id, 0.00, 'UNPAID', (CURRENT_DATE));
END$$

DELIMITER ;