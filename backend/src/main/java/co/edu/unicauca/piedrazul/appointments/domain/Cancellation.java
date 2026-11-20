package co.edu.unicauca.piedrazul.appointments.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Motivo y momento en que el paciente canceló la cita.
 */
public record Cancellation(String reason, Instant cancelledAt) {

    public Cancellation {
        Objects.requireNonNull(reason);
        Objects.requireNonNull(cancelledAt);
    }
}
