package co.edu.unicauca.piedrazul.appointments.application;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record BookAppointmentCommand(UUID patientId, UUID professionalId, LocalDate date, LocalTime startTime) {
}
