package co.edu.unicauca.piedrazul.appointments.infrastructure.web;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record RescheduleAppointmentRequest(
        @NotNull(message = "Elige una fecha.") LocalDate date,
        @NotNull(message = "Elige una hora.") LocalTime time) {
}
