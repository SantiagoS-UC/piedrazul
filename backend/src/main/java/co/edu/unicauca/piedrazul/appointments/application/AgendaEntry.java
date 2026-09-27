package co.edu.unicauca.piedrazul.appointments.application;

import co.edu.unicauca.piedrazul.appointments.domain.Appointment;
import co.edu.unicauca.piedrazul.identity.PatientSummary;
import java.util.Optional;

/**
 * Una cita del listado con los datos de contacto del paciente. El paciente puede faltar si su
 * registro ya no existe; la cita se muestra igual para que el total cuadre
 */
public record AgendaEntry(Appointment appointment, Optional<PatientSummary> patient) {
}
