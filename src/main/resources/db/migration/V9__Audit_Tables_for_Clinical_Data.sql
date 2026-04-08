-- ========================================================
-- V9: AUDIT TABLES FOR CLINICAL DATA (ENVERS)
-- ========================================================

CREATE TABLE prescriptions_aud (
                                   id BIGINT NOT NULL,
                                   rev INT NOT NULL,
                                   revtype TINYINT,
                                   appointment_id BIGINT,
                                   diagnosis TEXT,
                                   chief_complaints TEXT,
                                   advice TEXT,
                                   next_visit_date DATE,
                                   PRIMARY KEY (id, rev),
                                   CONSTRAINT fk_pres_aud_revinfo FOREIGN KEY (rev) REFERENCES revinfo (rev)
);

CREATE TABLE prescription_medicines_aud (
                                            id BIGINT NOT NULL,
                                            rev INT NOT NULL,
                                            revtype TINYINT,
                                            prescription_id BIGINT,
                                            medicine_id BIGINT,
                                            dosage VARCHAR(100),
                                            days INT,
                                            PRIMARY KEY (id, rev),
                                            CONSTRAINT fk_pm_aud_revinfo FOREIGN KEY (rev) REFERENCES revinfo (rev)
);