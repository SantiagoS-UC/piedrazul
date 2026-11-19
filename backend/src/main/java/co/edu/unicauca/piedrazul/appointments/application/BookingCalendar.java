package co.edu.unicauca.piedrazul.appointments.application;

import co.edu.unicauca.piedrazul.appointments.domain.BookingPeriod;
import co.edu.unicauca.piedrazul.appointments.domain.ProfessionalSchedule;
import co.edu.unicauca.piedrazul.professionals.ProfessionalSummary;

/**
 * Lo que el paciente necesita para elegir fecha: qué días atiende el profesional y hasta cuándo
 * puede agendar.
 */
public record BookingCalendar(ProfessionalSummary professional, BookingPeriod period, ProfessionalSchedule schedule) {
}
