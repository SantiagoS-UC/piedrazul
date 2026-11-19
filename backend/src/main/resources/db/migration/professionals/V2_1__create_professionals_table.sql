-- Tabla del módulo professionals. Los demás módulos la consultan solo a través de ProfessionalCatalog.

CREATE TABLE professionals (
    id         UUID PRIMARY KEY,
    first_name VARCHAR(60) NOT NULL,
    last_name  VARCHAR(60) NOT NULL,
    specialty  VARCHAR(30) NOT NULL
        CHECK (specialty IN ('GENERAL_MEDICINE', 'NEURAL_THERAPY', 'CHIROPRACTIC', 'PHYSIOTHERAPY')),
    active     BOOLEAN     NOT NULL DEFAULT TRUE
);
