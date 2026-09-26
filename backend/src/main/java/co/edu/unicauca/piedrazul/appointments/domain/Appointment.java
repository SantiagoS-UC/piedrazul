package co.edu.unicauca.piedrazul.appointments.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Cita de un paciente con un profesional. Guarda su propia duración para que un cambio posterior
 * en la configuración del profesional no altere las citas ya agendadas.
 */
public class Appointment {

    private final UUID id;
    private final UUID patientId;
    private final UUID professionalId;
    private final LocalDate date;
    private final LocalTime startTime;
    private final int durationMinutes;
    private final Instant createdAt;

    private Appointment(UUID id, UUID patientId, UUID professionalId, LocalDate date, LocalTime startTime,
            int durationMinutes, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.patientId = Objects.requireNonNull(patientId);
        this.professionalId = Objects.requireNonNull(professionalId);
        this.date = Objects.requireNonNull(date);
        this.startTime = Objects.requireNonNull(startTime);
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("La duración de la cita debe ser positiva.");
        }
        this.durationMinutes = durationMinutes;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static Appointment book(UUID id, UUID patientId, UUID professionalId, LocalDate date,
            LocalTime startTime, int durationMinutes, Instant createdAt) {
        return new Appointment(id, patientId, professionalId, date, startTime, durationMinutes, createdAt);
    }

    public static Appointment restore(UUID id, UUID patientId, UUID professionalId, LocalDate date,
            LocalTime startTime, int durationMinutes, Instant createdAt) {
        return new Appointment(id, patientId, professionalId, date, startTime, durationMinutes, createdAt);
    }

    /**
     * Indica si esta cita se cruza con un bloque de tiempo. Se compara por intervalos y no solo por la
     * hora de inicio porque el intervalo del profesional puede haber cambiado desde que se agendó.
     */
    public boolean overlaps(LocalDate otherDate, LocalTime otherStart, int otherMinutes) {
        if (!date.equals(otherDate)) {
            return false;
        }
        int start = minutesOfDay(startTime);
        int end = start + durationMinutes;
        int otherStartMinute = minutesOfDay(otherStart);
        return otherStartMinute < end && start < otherStartMinute + otherMinutes;
    }

    public LocalTime endTime() {
        return startTime.plusMinutes(durationMinutes);
    }

    public UUID id() {
        return id;
    }

    public UUID patientId() {
        return patientId;
    }

    public UUID professionalId() {
        return professionalId;
    }

    public LocalDate date() {
        return date;
    }

    public LocalTime startTime() {
        return startTime;
    }

    public int durationMinutes() {
        return durationMinutes;
    }

    public Instant createdAt() {
        return createdAt;
    }

    private static int minutesOfDay(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }
}
