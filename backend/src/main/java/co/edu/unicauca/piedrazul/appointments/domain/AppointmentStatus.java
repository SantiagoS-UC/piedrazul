package co.edu.unicauca.piedrazul.appointments.domain;

public enum AppointmentStatus {
    /** Vigente: ocupa su franja. */
    SCHEDULED,
    /** El paciente la canceló; la franja queda libre para otra persona. */
    CANCELLED
}
