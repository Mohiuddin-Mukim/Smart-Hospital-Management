-- ১. রিভিশন ইনফো টেবিল (Envers-এর গ্লোবাল ট্র্যাকার)
CREATE TABLE revinfo (
                         rev INT NOT NULL AUTO_INCREMENT,
                         revtstmp BIGINT,
                         PRIMARY KEY (rev)
);

-- ২. ইউজার অডিট টেবিল
CREATE TABLE users_aud (
                           id BIGINT NOT NULL,
                           rev INT NOT NULL,
                           revtype TINYINT, -- ০: যোগ, ১: আপডেট, ২: ডিলিট
                           email VARCHAR(150),
                           role VARCHAR(20),
                           is_active BOOLEAN,
                           email_verified BOOLEAN,
    -- পাসওয়ার্ড অডিট করার দরকার নেই তাই রাখা হয়নি
                           PRIMARY KEY (id, rev),
                           CONSTRAINT fk_users_aud_revinfo FOREIGN KEY (rev) REFERENCES revinfo (rev)
);

-- ৩. ডক্টর অডিট টেবিল
CREATE TABLE doctors_aud (
                             id BIGINT NOT NULL,
                             rev INT NOT NULL,
                             revtype TINYINT,
                             user_id BIGINT,
                             name VARCHAR(255),
                             specialization VARCHAR(255),
                             phone VARCHAR(255),
                             room_no VARCHAR(255),
                             is_active BOOLEAN,
                             PRIMARY KEY (id, rev),
                             CONSTRAINT fk_doctors_aud_revinfo FOREIGN KEY (rev) REFERENCES revinfo (rev)
);

-- ৪. পেশেন্ট অডিট টেবিল
CREATE TABLE patients_aud (
                              id BIGINT NOT NULL,
                              rev INT NOT NULL,
                              revtype TINYINT,
                              user_id BIGINT,
                              name VARCHAR(255),
                              age INT,
                              gender VARCHAR(50),
                              phone VARCHAR(50),
                              address TEXT,
                              is_active BOOLEAN,
                              PRIMARY KEY (id, rev),
                              CONSTRAINT fk_patients_aud_revinfo FOREIGN KEY (rev) REFERENCES revinfo (rev)
);

-- ৫. অ্যাপয়েন্টমেন্ট অডিট টেবিল
CREATE TABLE appointments_aud (
                                  id BIGINT NOT NULL,
                                  rev INT NOT NULL,
                                  revtype TINYINT,
                                  patient_id BIGINT,
                                  doctor_id BIGINT,
                                  date DATE,
                                  time TIME,
                                  status VARCHAR(50),
                                  reason TEXT,
                                  fee DECIMAL(10, 2),
                                  PRIMARY KEY (id, rev),
                                  CONSTRAINT fk_appointments_aud_revinfo FOREIGN KEY (rev) REFERENCES revinfo (rev)
);

-- ৬. ডক্টর শিডিউল অডিট টেবিল
CREATE TABLE doctor_schedules_aud (
                                      id BIGINT NOT NULL,
                                      rev INT NOT NULL,
                                      revtype TINYINT,
                                      doctor_id BIGINT,
                                      day_of_week VARCHAR(50),
                                      start_time TIME,
                                      end_time TIME,
                                      slot_duration INT,
                                      max_patients INT,
                                      is_active BOOLEAN,
                                      PRIMARY KEY (id, rev),
                                      CONSTRAINT fk_schedules_aud_revinfo FOREIGN KEY (rev) REFERENCES revinfo (rev)
);