package co.edu.unicauca.piedrazul.appointments.application;

import co.edu.unicauca.piedrazul.appointments.domain.Appointment;
import co.edu.unicauca.piedrazul.appointments.domain.BookingPeriod;
import co.edu.unicauca.piedrazul.appointments.domain.ProfessionalSchedule;
import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.UUID;

/**
 * Reglas para ocupar una franja, comunes a agendar y a reagendar una cita.
 */
final class SlotRules {

    private SlotRules() {
    }

    /**
     * La franja debe estar en el calendario del profesional, dentro de la ventana y todavía no haber
     * empezado.
     */
    static void checkOffered(ProfessionalSchedule schedule, BookingPeriod period, LocalDate date, LocalTime time,
            LocalDateTime now) {
        if (!period.includes(date) || !schedule.worksOn(date)) {
            throw new BusinessRuleViolationException("date", "Elige una fecha disponible del calendario.");
        }
        if (!schedule.offersSlotAt(time)) {
            throw new BusinessRuleViolationException("time", "Elige una de las horas disponibles.");
        }
        if (!date.atTime(time).isAfter(now)) {
            throw new BusinessRuleViolationException("time", "Esa hora ya pasó. Elige una hora más tarde.");
        }
    }

    /**
     * @param ignoredId cita que se está moviendo, que no choca consigo misma; {@code null} al agendar
     */
    static boolean anyOverlaps(Collection<Appointment> appointments, LocalDate date, LocalTime time, int minutes,
            UUID ignoredId) {
        return appointments.stream()
                .filter(appointment -> !appointment.id().equals(ignoredId))
                .anyMatch(appointment -> appointment.overlaps(date, time, minutes));
    }
}
