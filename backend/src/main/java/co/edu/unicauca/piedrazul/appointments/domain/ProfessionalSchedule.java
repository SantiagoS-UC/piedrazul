package co.edu.unicauca.piedrazul.appointments.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Horario de atención de un profesional tal como lo necesita el agendamiento.
 *
 * @param slotStartTimes horas de inicio de las franjas de un día de atención, en orden
 */
public record ProfessionalSchedule(Set<DayOfWeek> workingDays, List<LocalTime> slotStartTimes, int slotMinutes) {

    public ProfessionalSchedule {
        workingDays = Set.copyOf(workingDays);
        slotStartTimes = List.copyOf(slotStartTimes);
        if (slotMinutes <= 0) {
            throw new IllegalArgumentException("El intervalo entre citas debe ser positivo.");
        }
    }

    public boolean worksOn(LocalDate date) {
        return workingDays.contains(date.getDayOfWeek());
    }

    public boolean offersSlotAt(LocalTime time) {
        return slotStartTimes.contains(time);
    }

    /**
     * Franjas de una fecha con su estado. Una franja que ya pasó se marca como pasada aunque esté
     * ocupada, porque para el paciente lo importante es que ya no puede elegirla.
     *
     * @param booked citas del profesional en esa fecha
     * @param now    fecha y hora actuales en la zona horaria de la clínica
     */
    public List<Slot> slotsOn(LocalDate date, Collection<Appointment> booked, LocalDateTime now) {
        if (!worksOn(date)) {
            return List.of();
        }
        return slotStartTimes.stream()
                .map(start -> new Slot(start, statusOf(date, start, booked, now)))
                .toList();
    }

    private SlotStatus statusOf(LocalDate date, LocalTime start, Collection<Appointment> booked,
            LocalDateTime now) {
        if (!date.atTime(start).isAfter(now)) {
            return SlotStatus.PAST;
        }
        boolean taken = booked.stream().anyMatch(appointment -> appointment.overlaps(date, start, slotMinutes));
        return taken ? SlotStatus.TAKEN : SlotStatus.AVAILABLE;
    }
}
