-- ========================================================
-- V8: ENHANCED PRESCRIPTION & CLINICAL DATA
-- ========================================================

-- ১. প্রেসক্রিপশন টেবিলে ক্লিনিক্যাল তথ্য যোগ করা
ALTER TABLE prescriptions
    ADD COLUMN weight VARCHAR(10) NULL AFTER appointment_id,
    ADD COLUMN blood_pressure VARCHAR(20) NULL AFTER weight,
    ADD COLUMN temperature VARCHAR(10) NULL AFTER blood_pressure,
    ADD COLUMN pulse VARCHAR(10) NULL AFTER temperature,
    ADD COLUMN chief_complaints TEXT NULL AFTER pulse, -- রোগীর প্রধান সমস্যাগুলো
    ADD COLUMN clinical_findings TEXT NULL AFTER chief_complaints, -- ডাক্তার যা পরীক্ষা করে পেলেন
    ADD COLUMN advice TEXT NULL AFTER notes, -- জীবনযাত্রার পরামর্শ
    ADD COLUMN next_visit_date DATE NULL AFTER advice;

-- ২. প্রেসক্রিপশন মেডিসিন টেবিলে স্পষ্টতা আনা
ALTER TABLE prescription_medicines
    MODIFY COLUMN dosage VARCHAR(100) NOT NULL, -- উদা: 1+0+1
    ADD COLUMN instruction VARCHAR(255) NULL,   -- উদা: খাওয়ার আগে, চিবিয়ে খাবেন
    ADD COLUMN days INT NOT NULL DEFAULT 0,     -- কত দিন খাবে
    ADD COLUMN is_continued TINYINT(1) DEFAULT 0; -- আগে থেকে চলা ঔষধ কি না

-- ৩. বিলিং ট্রিগার আপডেট (অ্যাপয়েন্টমেন্ট ফি অটোমেটিক বিলে নেওয়ার জন্য)
DROP TRIGGER IF EXISTS trg_create_bill;

DELIMITER $$
CREATE TRIGGER trg_create_bill
    AFTER INSERT ON prescriptions
    FOR EACH ROW
BEGIN
    DECLARE p_id BIGINT;
    DECLARE appt_fee DECIMAL(12,2);

    -- অ্যাপয়েন্টমেন্ট থেকে পেশেন্ট আইডি এবং ফি খুঁজে বের করা
    SELECT patient_id, fee INTO p_id, appt_fee
    FROM appointments
    WHERE id = NEW.appointment_id;

    -- বিল তৈরি করা (ফি যদি ০ থাকে তবে আপাতত ০ বসবে)
    INSERT INTO bills (patient_id, appointment_id, amount, status, `date`)
    VALUES (p_id, NEW.appointment_id, appt_fee, 'UNPAID', CURRENT_DATE);
    END$$
    DELIMITER ;