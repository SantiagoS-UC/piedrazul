package co.edu.unicauca.piedrazul.appointments.application;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.WEDNESDAY;

import co.edu.unicauca.piedrazul.configuration.AvailabilitySummary;
import co.edu.unicauca.piedrazul.professionals.ProfessionalSummary;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Datos compartidos por las pruebas del agendamiento. "Hoy" es el lunes 28 de septiembre de 2026 a
 * las 9:10 a. m., hora de Colombia.
 */
final class BookingFixtures {

    static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-28T14:10:00Z"), ZoneId.of("America/Bogota"));
    static final LocalDate TODAY = LocalDate.of(2026, 9, 28);
    static final LocalDate LAST_BOOKABLE_DATE = TODAY.plusWeeks(4);
    static final LocalDate NEXT_WEDNESDAY = LocalDate.of(2026, 9, 30);
    static final LocalDate NEXT_TUESDAY = LocalDate.of(2026, 9, 29);

    static final ProfessionalSummary LAURA = professional("Laura", "Martínez", "GENERAL_MEDICINE",
            "Medicina general", true);
    static final ProfessionalSummary ANDRES = professional("Andrés", "Vargas", "GENERAL_MEDICINE",
            "Medicina general", true);
    static final ProfessionalSummary DIANA = professional("Diana", "Rojas", "NEURAL_THERAPY",
            "Terapia neural", true);

    private BookingFixtures() {
    }

    static ProfessionalSummary professional(String firstName, String lastName, String specialty,
            String specialtyName, boolean active) {
        return new ProfessionalSummary(UUID.randomUUID(), firstName, lastName, specialty, specialtyName, active);
    }

    /**
     * Lunes y miércoles de 8:00 a 12:00, cada 30 minutos.
     */
    static AvailabilitySummary availabilityOf(ProfessionalSummary professional) {
        List<LocalTime> slots = List.of(LocalTime.of(8, 0), LocalTime.of(8, 30), LocalTime.of(9, 0),
                LocalTime.of(9, 30), LocalTime.of(10, 0), LocalTime.of(10, 30), LocalTime.of(11, 0),
                LocalTime.of(11, 30));
        return new AvailabilitySummary(professional.id(), Set.of(MONDAY, WEDNESDAY), LocalTime.of(8, 0),
                LocalTime.of(12, 0), 30, slots);
    }
}
