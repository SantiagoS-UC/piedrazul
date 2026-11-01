package co.edu.unicauca.piedrazul.identity.domain;

public enum Role {
    PATIENT,
    SCHEDULER,
    /**
     * Médico o terapista. Su cuenta comparte el identificador con su registro en el módulo
     * professionals, así el token dice directamente de qué profesional se trata.
     */
    PROFESSIONAL,
    ADMIN
}
