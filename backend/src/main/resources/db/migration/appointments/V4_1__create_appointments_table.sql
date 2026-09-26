-- Tabla del módulo appointments. patient_id y professional_id apuntan a otros módulos, pero sin
-- llave foránea: cada módulo es dueño de sus propias tablas.
--
-- Las restricciones únicas son la última defensa cuando dos personas confirman la misma franja
-- al mismo tiempo; AppointmentRepositoryAdapter depende de sus nombres.

CREATE TABLE appointments (
    id               UUID PRIMARY KEY,
    patient_id       UUID        NOT NULL,
    professional_id  UUID        NOT NULL,
    appointment_date DATE        NOT NULL,
    start_time       TIME        NOT NULL,
    duration_minutes INTEGER     NOT NULL CHECK (duration_minutes > 0),
    created_at       TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_appointments_professional_slot UNIQUE (professional_id, appointment_date, start_time),
    CONSTRAINT uq_appointments_patient_slot UNIQUE (patient_id, appointment_date, start_time)
);
