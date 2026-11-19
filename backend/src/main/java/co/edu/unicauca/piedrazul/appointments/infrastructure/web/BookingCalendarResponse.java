package co.edu.unicauca.piedrazul.appointments.infrastructure.web;

import co.edu.unicauca.piedrazul.appointments.application.BookingCalendar;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * @param firstDate primer día que se puede elegir (hoy)
 * @param lastDate  último día que se puede elegir según la ventana de agendamiento
 */
public record BookingCalendarResponse(
        BookableProfessionalResponse professional,
        LocalDate firstDate,
        LocalDate lastDate,
        Set<DayOfWeek> workingDays,
        int slotMinutes) {

    static BookingCalendarResponse from(BookingCalendar calendar) {
        return new BookingCalendarResponse(
                BookableProfessionalResponse.from(calendar.professional()),
                calendar.period().firstDate(),
                calendar.period().lastDate(),
                calendar.schedule().workingDays(),
                calendar.schedule().slotMinutes());
    }
}
