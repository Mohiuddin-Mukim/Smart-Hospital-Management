ALTER TABLE queue_tracker
    ADD COLUMN currently_serving INT DEFAULT 0;
DROP TRIGGER IF EXISTS trg_complete_appointment;

DELIMITER $$

CREATE TRIGGER trg_update_queue_and_appointment
    AFTER UPDATE ON queue_tokens
    FOR EACH ROW
BEGIN
    IF OLD.status <> 'INSIDE' AND NEW.status = 'INSIDE' THEN
    UPDATE queue_tracker qt
        JOIN appointments a ON a.id = NEW.appointment_id
        SET qt.currently_serving = NEW.serial_no
    WHERE qt.doctor_id = a.doctor_id
      AND qt.date = a.date;
END IF;

IF OLD.status <> 'DONE' AND NEW.status = 'DONE' THEN
UPDATE appointments
SET status = 'COMPLETED'
WHERE id = NEW.appointment_id;
END IF;
END$$

DELIMITER ;