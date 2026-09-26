package co.edu.unicauca.piedrazul.professionals.domain;

/**
 * Especialidades que ofrece Piedrazul. Medicina general la atienden médicos; las demás, terapistas.
 */
public enum Specialty {
    GENERAL_MEDICINE("Medicina general", ProfessionalType.DOCTOR),
    NEURAL_THERAPY("Terapia neural", ProfessionalType.THERAPIST),
    CHIROPRACTIC("Quiropraxia", ProfessionalType.THERAPIST),
    PHYSIOTHERAPY("Fisioterapia", ProfessionalType.THERAPIST);

    private final String displayName;
    private final ProfessionalType professionalType;

    Specialty(String displayName, ProfessionalType professionalType) {
        this.displayName = displayName;
        this.professionalType = professionalType;
    }

    public String displayName() {
        return displayName;
    }

    public ProfessionalType professionalType() {
        return professionalType;
    }
}
