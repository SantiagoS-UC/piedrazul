package co.edu.unicauca.piedrazul.professionals.infrastructure.web;

import co.edu.unicauca.piedrazul.professionals.ProfessionalSummary;
import java.util.UUID;

record ProfessionalResponse(UUID id, String firstName, String lastName, String specialty, String specialtyName,
        boolean active) {

    static ProfessionalResponse from(ProfessionalSummary summary) {
        return new ProfessionalResponse(summary.id(), summary.firstName(), summary.lastName(), summary.specialty(),
                summary.specialtyName(), summary.active());
    }
}
