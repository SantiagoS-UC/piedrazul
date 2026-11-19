package co.edu.unicauca.piedrazul.configuration.infrastructure.web;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

public record AvailabilityRequest(
        @NotEmpty(message = "Selecciona al menos un día de atención.") Set<DayOfWeek> workingDays,
        @NotNull(message = "Escribe la hora de inicio.") LocalTime startTime,
        @NotNull(message = "Escribe la hora de fin.") LocalTime endTime,
        @NotNull(message = "Escribe el intervalo entre citas.") Integer slotMinutes) {
}
