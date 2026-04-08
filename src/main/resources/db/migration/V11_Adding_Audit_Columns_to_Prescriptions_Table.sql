-- ========================================================
-- V12: ADD AUDIT COLUMNS TO PRESCRIPTIONS TABLE
-- ========================================================

ALTER TABLE prescriptions
    ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- একই সাথে prescription_medicines টেবিলেও এই কলামগুলো যোগ করে রাখা ভালো (ভবিষ্যতের জন্য)
ALTER TABLE prescription_medicines
    ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;