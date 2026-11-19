-- Tablas del módulo identity. Ningún otro módulo las consulta directamente: usan PatientDirectory.

CREATE TABLE users (
    id            UUID PRIMARY KEY,
    email         VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('PATIENT', 'SCHEDULER', 'ADMIN')),
    display_name  VARCHAR(120) NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL
);

CREATE TABLE patients (
    id               UUID PRIMARY KEY REFERENCES users (id),
    first_name       VARCHAR(60) NOT NULL,
    middle_name      VARCHAR(60),
    first_last_name  VARCHAR(60) NOT NULL,
    second_last_name VARCHAR(60),
    document_type    VARCHAR(2)  NOT NULL CHECK (document_type IN ('CC', 'TI', 'CE', 'PA')),
    document_number  VARCHAR(20) NOT NULL,
    birth_date       DATE        NOT NULL,
    gender           VARCHAR(10) NOT NULL CHECK (gender IN ('FEMALE', 'MALE', 'OTHER')),
    phone            VARCHAR(10) NOT NULL,
    CONSTRAINT uq_patients_document UNIQUE (document_type, document_number)
);
