package co.edu.unicauca.piedrazul.appointments.application;

import java.util.UUID;

/**
 * @param patientId paciente que hace la petición, tomado del token
 */
public record CancelAppointmentCommand(UUID patientId, UUID appointmentId, String reason) {
}
