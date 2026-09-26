package co.edu.unicauca.piedrazul.appointments.application;

import co.edu.unicauca.piedrazul.appointments.domain.Appointment;
import co.edu.unicauca.piedrazul.professionals.ProfessionalSummary;

/**
 * Resumen que se muestra al paciente al confirmar la cita.
 */
public record BookedAppointment(Appointment appointment, ProfessionalSummary professional) {
}
