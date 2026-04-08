-- ১. prescriptions_aud টেবিল আপডেট (মিসিং কলামগুলো যোগ করা)
ALTER TABLE prescriptions_aud
    ADD COLUMN weight VARCHAR(10),
ADD COLUMN blood_pressure VARCHAR(20),
ADD COLUMN temperature VARCHAR(10),
ADD COLUMN pulse VARCHAR(10),
ADD COLUMN clinical_findings TEXT,
ADD COLUMN notes TEXT;

-- ২. prescription_medicines_aud টেবিল আপডেট (মিসিং কলামগুলো যোগ করা)
ALTER TABLE prescription_medicines_aud
    ADD COLUMN brand_id BIGINT,
ADD COLUMN duration VARCHAR(50),
ADD COLUMN instruction VARCHAR(255),
ADD COLUMN is_continued TINYINT(1) DEFAULT 0;