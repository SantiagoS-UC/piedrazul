package co.edu.unicauca.piedrazul.appointments.infrastructure.web;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record StaffBookAppointmentRequest(
        @NotNull(message = "Elige un paciente.") UUID patientId,
        @NotNull(message = "Elige un profesional.") UUID professionalId,
        @NotNull(message = "Elige una fecha.") LocalDate date,
        @NotNull(message = "Elige una hora.") LocalTime time) {
}
