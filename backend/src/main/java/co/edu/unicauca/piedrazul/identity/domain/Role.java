package co.edu.unicauca.piedrazul.identity.domain;

public enum Role {
    PATIENT,
    SCHEDULER,
    /**
     * Médico o terapista. El id de su cuenta es el mismo de su registro en professionals.
     */
    PROFESSIONAL,
    ADMIN
}
