-- ========================================================
-- V2: PRODUCTION-GRADE PAYMENT & WALLET SYSTEM
-- ========================================================

USE management;

-- 1. WALLET SYSTEM
ALTER TABLE patients
    ADD COLUMN wallet_balance DECIMAL(12,2) NOT NULL DEFAULT 0.00 AFTER address;

-- 2. APPOINTMENT IMPROVEMENT
ALTER TABLE appointments
    ADD COLUMN fee DECIMAL(12,2) NOT NULL DEFAULT 0.00 AFTER reason,
MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

-- Strict slot uniqueness to prevent double booking
ALTER TABLE appointments
    ADD CONSTRAINT uq_doctor_slot UNIQUE (doctor_id, `date`, `time`);

-- 3. PAYMENTS TABLE
CREATE TABLE payments (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          appointment_id BIGINT NOT NULL UNIQUE,
                          patient_id BIGINT NOT NULL,
                          invoice_no VARCHAR(50) UNIQUE,
                          amount DECIMAL(12,2) NOT NULL,
                          status VARCHAR(20) NOT NULL, -- 'INITIATED','SUCCESS','FAILED','REFUNDED'
                          transaction_id VARCHAR(100) UNIQUE,
                          provider_reference VARCHAR(100),
                          payment_method VARCHAR(50) DEFAULT 'BKASH',
                          refunded_at TIMESTAMP NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                          CHECK (status IN ('INITIATED','SUCCESS','FAILED','REFUNDED')),
                          CONSTRAINT fk_payment_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
                          CONSTRAINT fk_payment_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 4. PAYMENT TRANSACTION AUDIT (Double Entry Audit)
CREATE TABLE payment_transactions (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      payment_id BIGINT NOT NULL,
                                      type VARCHAR(20) NOT NULL, -- 'DEBIT','CREDIT'
                                      amount DECIMAL(12,2) NOT NULL,
                                      balance_after DECIMAL(12,2) NOT NULL,
                                      description VARCHAR(255),
                                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                      CHECK (type IN ('DEBIT','CREDIT')),
                                      CONSTRAINT fk_tx_payment FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 5. INDEXING (Performance Optimized)
CREATE INDEX idx_payment_status ON payments(status);
CREATE INDEX idx_payment_patient ON payments(patient_id);
CREATE INDEX idx_invoice_no ON payments(invoice_no);
CREATE INDEX idx_appointment_status ON appointments(status);
CREATE INDEX idx_appointment_date ON appointments(`date`);