DELETE FROM doctor_schedules;

ALTER TABLE doctor_schedules
    ADD CONSTRAINT uq_doctor_session UNIQUE (doctor_id, day_of_week, start_time);

CREATE INDEX idx_appt_lookup ON appointments(doctor_id, `date`, `time`);