package co.edu.unicauca.piedrazul.appointments.domain;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Fechas en las que un paciente puede agendar: desde hoy hasta el final de la ventana configurada.
 */
public record BookingPeriod(LocalDate firstDate, LocalDate lastDate) {

    public BookingPeriod {
        Objects.requireNonNull(firstDate);
        Objects.requireNonNull(lastDate);
        if (lastDate.isBefore(firstDate)) {
            throw new IllegalArgumentException("El periodo termina antes de empezar.");
        }
    }

    public boolean includes(LocalDate date) {
        return !date.isBefore(firstDate) && !date.isAfter(lastDate);
    }
}
