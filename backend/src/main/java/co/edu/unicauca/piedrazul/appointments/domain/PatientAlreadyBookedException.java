package co.edu.unicauca.piedrazul.appointments.domain;

import co.edu.unicauca.piedrazul.shared.domain.DuplicateResourceException;

public class PatientAlreadyBookedException extends DuplicateResourceException {

    public PatientAlreadyBookedException() {
        super("time", "Ya tienes una cita a esa hora. Elige otro horario.");
    }
}
