package co.edu.unicauca.piedrazul.appointments.infrastructure.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelAppointmentRequest(
        @NotBlank(message = "Escribe el motivo de la cancelación.")
        @Size(max = 500, message = "El motivo puede tener máximo 500 caracteres.")
        String reason) {
}
