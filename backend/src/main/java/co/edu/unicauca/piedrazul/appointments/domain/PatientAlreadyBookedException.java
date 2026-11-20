package co.edu.unicauca.piedrazul.appointments.domain;

import co.edu.unicauca.piedrazul.shared.domain.DuplicateResourceException;

public class PatientAlreadyBookedException extends DuplicateResourceException {

    public PatientAlreadyBookedException() {
        this("Ya tienes una cita a esa hora. Elige otro horario.");
    }

    private PatientAlreadyBookedException(String message) {
        super("time", message);
    }

    /**
     * El mismo error, redactado para el agendador o el profesional que maneja la cita del paciente.
     */
    public static PatientAlreadyBookedException forStaff() {
        return new PatientAlreadyBookedException("El paciente ya tiene una cita a esa hora. Elige otro horario.");
    }
}
