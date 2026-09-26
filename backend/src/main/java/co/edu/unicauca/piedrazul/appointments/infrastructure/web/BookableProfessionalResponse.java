package co.edu.unicauca.piedrazul.appointments.infrastructure.web;

import co.edu.unicauca.piedrazul.professionals.ProfessionalSummary;
import java.util.UUID;

public record BookableProfessionalResponse(UUID id, String firstName, String lastName, String specialty,
        String specialtyName) {

    static BookableProfessionalResponse from(ProfessionalSummary professional) {
        return new BookableProfessionalResponse(professional.id(), professional.firstName(),
                professional.lastName(), professional.specialty(), professional.specialtyName());
    }
}
