-- RF6: el paciente puede cancelar una cita. La cita no se borra para que siga en el historial con su
-- motivo, pero una cita cancelada no debe impedir que otra persona tome la misma franja.
--
-- Por eso las restricciones únicas pasan a ser índices únicos parciales que solo cuentan las citas
-- vigentes. Conservan los mismos nombres porque AppointmentRepositoryAdapter los reconoce por nombre.

ALTER TABLE appointments
    ADD COLUMN status              VARCHAR(20)  NOT NULL DEFAULT 'SCHEDULED'
        CHECK (status IN ('SCHEDULED', 'CANCELLED')),
    ADD COLUMN cancellation_reason VARCHAR(500),
    ADD COLUMN cancelled_at        TIMESTAMPTZ;

ALTER TABLE appointments DROP CONSTRAINT uq_appointments_professional_slot;
ALTER TABLE appointments DROP CONSTRAINT uq_appointments_patient_slot;

CREATE UNIQUE INDEX uq_appointments_professional_slot
    ON appointments (professional_id, appointment_date, start_time)
    WHERE status = 'SCHEDULED';

CREATE UNIQUE INDEX uq_appointments_patient_slot
    ON appointments (patient_id, appointment_date, start_time)
    WHERE status = 'SCHEDULED';
