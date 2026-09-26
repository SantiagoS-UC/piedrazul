package co.edu.unicauca.piedrazul.configuration.domain;

import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Días, franja horaria e intervalo entre citas de un médico o terapista.
 */
public class ProfessionalAvailability {

    public static final int MIN_SLOT_MINUTES = 10;
    public static final int MAX_SLOT_MINUTES = 120;

    private final UUID professionalId;
    private final Set<DayOfWeek> workingDays;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final int slotMinutes;

    public ProfessionalAvailability(UUID professionalId, Set<DayOfWeek> workingDays, LocalTime startTime,
            LocalTime endTime, int slotMinutes) {
        this.professionalId = Objects.requireNonNull(professionalId);
        this.workingDays = validDays(workingDays);
        this.startTime = requireTime("startTime", startTime, "Escribe la hora de inicio.");
        this.endTime = requireTime("endTime", endTime, "Escribe la hora de fin.");
        if (!this.endTime.isAfter(this.startTime)) {
            throw new BusinessRuleViolationException("endTime",
                    "La hora de fin debe ser posterior a la hora de inicio.");
        }
        if (slotMinutes < MIN_SLOT_MINUTES || slotMinutes > MAX_SLOT_MINUTES) {
            throw new BusinessRuleViolationException("slotMinutes",
                    "El intervalo debe estar entre " + MIN_SLOT_MINUTES + " y " + MAX_SLOT_MINUTES + " minutos.");
        }
        if (slotMinutes > minutesOfDay(this.endTime) - minutesOfDay(this.startTime)) {
            throw new BusinessRuleViolationException("slotMinutes",
                    "El intervalo no puede ser mayor que la franja horaria.");
        }
        this.slotMinutes = slotMinutes;
    }

    public boolean worksOn(DayOfWeek day) {
        return workingDays.contains(day);
    }

    /**
     * Horas de inicio de las franjas de un día. Solo se incluye una franja si termina a más tardar
     * a la hora de fin: de 8:00 a 12:00 con 45 minutos, la última es a las 11:00.
     */
    public List<LocalTime> slotStartTimes() {
        // Se calcula en minutos del día y no sumando a LocalTime, que daría la vuelta a medianoche.
        int end = minutesOfDay(endTime);
        List<LocalTime> slots = new ArrayList<>();
        for (int minute = minutesOfDay(startTime); minute + slotMinutes <= end; minute += slotMinutes) {
            slots.add(LocalTime.of(minute / 60, minute % 60));
        }
        return Collections.unmodifiableList(slots);
    }

    public UUID professionalId() {
        return professionalId;
    }

    public Set<DayOfWeek> workingDays() {
        return workingDays;
    }

    public LocalTime startTime() {
        return startTime;
    }

    public LocalTime endTime() {
        return endTime;
    }

    public int slotMinutes() {
        return slotMinutes;
    }

    private static Set<DayOfWeek> validDays(Set<DayOfWeek> days) {
        if (days == null || days.isEmpty()) {
            throw new BusinessRuleViolationException("workingDays", "Selecciona al menos un día de atención.");
        }
        if (days.contains(DayOfWeek.SUNDAY)) {
            throw new BusinessRuleViolationException("workingDays", "Los días de atención van de lunes a sábado.");
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(days));
    }

    private static LocalTime requireTime(String field, LocalTime time, String message) {
        if (time == null) {
            throw new BusinessRuleViolationException(field, message);
        }
        return time.truncatedTo(ChronoUnit.MINUTES);
    }

    private static int minutesOfDay(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }
}
