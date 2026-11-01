package co.edu.unicauca.piedrazul.appointments.application;

import co.edu.unicauca.piedrazul.appointments.domain.Appointment;
import co.edu.unicauca.piedrazul.professionals.ProfessionalSummary;
import java.util.Optional;

/**
 * @param professional vacío si el profesional ya no existe en el catálogo
 * @param upcoming     si la cita todavía no ha empezado
 */
public record PatientHistoryEntry(Appointment appointment, Optional<ProfessionalSummary> professional,
        boolean upcoming) {
}
