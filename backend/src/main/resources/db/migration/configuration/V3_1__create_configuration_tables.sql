-- Tablas del módulo configuration. professional_id apunta a un profesional del módulo
-- professionals, pero sin llave foránea: cada módulo es dueño de sus propias tablas.

CREATE TABLE scheduling_settings (
    id           INTEGER PRIMARY KEY CHECK (id = 1),
    window_weeks INTEGER NOT NULL CHECK (window_weeks BETWEEN 1 AND 12)
);

CREATE TABLE professional_availabilities (
    professional_id UUID PRIMARY KEY,
    working_days    VARCHAR(70) NOT NULL,
    start_time      TIME        NOT NULL,
    end_time        TIME        NOT NULL,
    slot_minutes    INTEGER     NOT NULL CHECK (slot_minutes BETWEEN 10 AND 120),
    CHECK (end_time > start_time)
);
