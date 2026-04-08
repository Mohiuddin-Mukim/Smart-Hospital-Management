-- ========================================================
-- V12: DOCTOR PROFILE ENHANCEMENT & SMART BILLING TRIGGER
-- ========================================================

-- ১. ডক্টর প্রোফাইল আপডেট (Main Table)
ALTER TABLE doctors
    ADD COLUMN degree VARCHAR(255) NULL AFTER specialization,
    ADD COLUMN designation VARCHAR(150) NULL AFTER degree,
    ADD COLUMN consultation_fee DECIMAL(12,2) NOT NULL DEFAULT 500.00 AFTER designation,
    ADD COLUMN about_doctor TEXT NULL AFTER consultation_fee,
    ADD COLUMN gender VARCHAR(10) NULL AFTER phone,
    ADD COLUMN profile_picture_url VARCHAR(255) NULL AFTER room_no,
    ADD COLUMN experience_years INT DEFAULT 0 AFTER profile_picture_url;

-- ২. ডক্টর অডিট টেবিল আপডেট (Envers-এর জন্য এটি বাধ্যতামূলক)
ALTER TABLE doctors_aud
    ADD COLUMN degree VARCHAR(255) NULL,
    ADD COLUMN designation VARCHAR(150) NULL,
    ADD COLUMN consultation_fee DECIMAL(12,2) NULL,
    ADD COLUMN about_doctor TEXT NULL,
    ADD COLUMN gender VARCHAR(10) NULL,
    ADD COLUMN profile_picture_url VARCHAR(255) NULL,
    ADD COLUMN experience_years INT NULL;

-- ৩. অ্যাপয়েন্টমেন্ট ফি কলামের ডিফল্ট ভ্যালু ১০০ করা (টোকেন মানি)
ALTER TABLE appointments
    MODIFY COLUMN fee DECIMAL(12,2) DEFAULT 100.00;

-- ৪. বিলিং ট্রিগার আপডেট (ডাক্তারের ফি থেকে টোকেন ১০০ মাইনাস করে বিল হবে)
DROP TRIGGER IF EXISTS trg_create_bill;

DELIMITER $$
CREATE TRIGGER trg_create_bill
    AFTER INSERT ON prescriptions
    FOR EACH ROW
BEGIN
    DECLARE p_id BIGINT;
    DECLARE d_id BIGINT;
    DECLARE token_paid DECIMAL(12,2);
    DECLARE total_doctor_fee DECIMAL(12,2);
    DECLARE net_payable DECIMAL(12,2);

    SELECT patient_id, doctor_id, fee INTO p_id, d_id, token_paid
    FROM appointments WHERE id = NEW.appointment_id;

    SELECT consultation_fee INTO total_doctor_fee
    FROM doctors WHERE id = d_id;

    -- ভ্যালিডেশন: যদি টোকেন ফি ভুল করে বেশি হয়ে যায়, তবে নেট পেয়াবল ০ হবে
    IF token_paid > total_doctor_fee THEN
        SET net_payable = 0.00;
    ELSE
        SET net_payable = total_doctor_fee - token_paid;
END IF;

INSERT INTO bills (patient_id, appointment_id, amount, status, `date`)
VALUES (p_id, NEW.appointment_id, net_payable, 'UNPAID', CURRENT_DATE);
END$$
DELIMITER ;