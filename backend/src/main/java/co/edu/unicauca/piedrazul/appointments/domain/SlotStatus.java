package co.edu.unicauca.piedrazul.appointments.domain;

public enum SlotStatus {
    AVAILABLE,
    /** Otro paciente ya tiene una cita en esa franja. */
    TAKEN,
    /** La franja es de hoy y su hora ya pasó. */
    PAST
}
