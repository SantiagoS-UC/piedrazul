package co.edu.unicauca.piedrazul.appointments.domain;

import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Cita de un paciente con un profesional. Guarda su propia duración para que un cambio posterior
 * en la configuración del profesional no altere las citas ya agendadas.
 *
 * <p>Una cita cancelada no se borra: queda en el historial con su motivo, pero deja de ocupar la franja.
 */
public class Appointment {

    /** RF6: se cancela con mínimo dos días de anticipación. */
    public static final int MIN_DAYS_BEFORE_CANCELLING = 2;
    public static final int MIN_REASON_LENGTH = 10;
    public static final int MAX_REASON_LENGTH = 500;

    private final UUID id;
    private final UUID patientId;
    private final UUID professionalId;
    private final LocalDate date;
    private final LocalTime startTime;
    private final int durationMinutes;
    private final Instant createdAt;
    /** {@code null} mientras la cita esté vigente. */
    private final Cancellation cancellation;

    private Appointment(UUID id, UUID patientId, UUID professionalId, LocalDate date, LocalTime startTime,
            int durationMinutes, Instant createdAt, Cancellation cancellation) {
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
        this.cancellation = cancellation;
    }

    public static Appointment book(UUID id, UUID patientId, UUID professionalId, LocalDate date,
            LocalTime startTime, int durationMinutes, Instant createdAt) {
        return new Appointment(id, patientId, professionalId, date, startTime, durationMinutes, createdAt, null);
    }

    /**
     * @param cancellation {@code null} si la cita está vigente
     */
    public static Appointment restore(UUID id, UUID patientId, UUID professionalId, LocalDate date,
            LocalTime startTime, int durationMinutes, Instant createdAt, Cancellation cancellation) {
        return new Appointment(id, patientId, professionalId, date, startTime, durationMinutes, createdAt,
                cancellation);
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

    /**
     * La misma cita en otra fecha u hora. Conserva el paciente, el profesional y la fecha de creación;
     * la duración se recibe porque se toma la configuración vigente del profesional.
     */
    public Appointment rescheduleTo(LocalDate newDate, LocalTime newStartTime, int newDurationMinutes) {
        if (isCancelled()) {
            throw new BusinessRuleViolationException("Esta cita fue cancelada y ya no se puede reagendar.");
        }
        return new Appointment(id, patientId, professionalId, newDate, newStartTime, newDurationMinutes, createdAt,
                null);
    }

    /**
     * Solo se pueden cancelar citas vigentes cuya fecha sea al menos dos días después de hoy: una cita
     * del miércoles se puede cancelar hasta el lunes.
     */
    public boolean canBeCancelledOn(LocalDate today) {
        return !isCancelled() && !date.isBefore(today.plusDays(MIN_DAYS_BEFORE_CANCELLING));
    }

    /**
     * RF6: el paciente cancela la cita dejando el motivo.
     *
     * @param today fecha actual en la zona horaria de la clínica
     */
    public Appointment cancel(String reason, Instant now, LocalDate today) {
        if (isCancelled()) {
            throw new BusinessRuleViolationException("Esta cita ya estaba cancelada.");
        }
        if (!canBeCancelledOn(today)) {
            throw new BusinessRuleViolationException("Las citas se cancelan con mínimo "
                    + MIN_DAYS_BEFORE_CANCELLING + " días de anticipación. Si no puedes asistir, comunícate con "
                    + "la clínica.");
        }
        String cleanReason = reason == null ? "" : reason.strip();
        if (cleanReason.isEmpty()) {
            throw new BusinessRuleViolationException("reason", "Escribe el motivo de la cancelación.");
        }
        if (cleanReason.length() < MIN_REASON_LENGTH) {
            throw new BusinessRuleViolationException("reason",
                    "Cuéntanos un poco más: el motivo debe tener al menos " + MIN_REASON_LENGTH + " caracteres.");
        }
        if (cleanReason.length() > MAX_REASON_LENGTH) {
            throw new BusinessRuleViolationException("reason",
                    "El motivo puede tener máximo " + MAX_REASON_LENGTH + " caracteres.");
        }
        return new Appointment(id, patientId, professionalId, date, startTime, durationMinutes, createdAt,
                new Cancellation(cleanReason, now));
    }

    public boolean isCancelled() {
        return cancellation != null;
    }

    public AppointmentStatus status() {
        return isCancelled() ? AppointmentStatus.CANCELLED : AppointmentStatus.SCHEDULED;
    }

    public Optional<Cancellation> cancellation() {
        return Optional.ofNullable(cancellation);
    }

    public boolean startsAt(LocalDate otherDate, LocalTime otherStart) {
        return date.equals(otherDate) && startTime.equals(otherStart);
    }

    public LocalTime endTime() {
        return startTime.plusMinutes(durationMinutes);
    }

    /**
     * Una cita deja de ser próxima en cuanto llega su hora de inicio.
     */
    public boolean isUpcomingAt(LocalDateTime now) {
        return date.atTime(startTime).isAfter(now);
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
