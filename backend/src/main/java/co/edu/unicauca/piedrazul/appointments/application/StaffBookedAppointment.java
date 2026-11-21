package co.edu.unicauca.piedrazul.appointments.application;

import co.edu.unicauca.piedrazul.identity.PatientSummary;

/**
 * Cita registrada por el agendador, con el paciente para mostrar a nombre de quién quedó.
 */
public record StaffBookedAppointment(BookedAppointment booked, PatientSummary patient) {
}
