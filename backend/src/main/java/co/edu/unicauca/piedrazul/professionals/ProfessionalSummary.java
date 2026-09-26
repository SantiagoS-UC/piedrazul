package co.edu.unicauca.piedrazul.professionals;

import java.util.UUID;

/**
 * @param specialty      código de la especialidad, por ejemplo GENERAL_MEDICINE
 * @param specialtyName  nombre de la especialidad para mostrar al usuario
 */
public record ProfessionalSummary(
        UUID id,
        String firstName,
        String lastName,
        String specialty,
        String specialtyName,
        boolean active) {

    public String fullName() {
        return firstName + " " + lastName;
    }
}
