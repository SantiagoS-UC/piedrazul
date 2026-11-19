package co.edu.unicauca.piedrazul.configuration.infrastructure.web;

import jakarta.validation.constraints.NotNull;

public record SchedulingWindowRequest(
        @NotNull(message = "Escribe el número de semanas.") Integer weeks) {
}
