package co.edu.unicauca.piedrazul.appointments.application;

import co.edu.unicauca.piedrazul.appointments.domain.BookingPeriod;
import co.edu.unicauca.piedrazul.appointments.domain.ProfessionalSchedule;
import co.edu.unicauca.piedrazul.configuration.AvailabilitySummary;
import co.edu.unicauca.piedrazul.configuration.SchedulingSettings;
import co.edu.unicauca.piedrazul.professionals.ProfessionalCatalog;
import co.edu.unicauca.piedrazul.professionals.ProfessionalSummary;
import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import co.edu.unicauca.piedrazul.shared.domain.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Reúne lo que el agendamiento necesita de los módulos professionals y configuration, traducido
 * al modelo de este módulo.
 */
@Component
class BookingSchedules {

    private final ProfessionalCatalog professionalCatalog;
    private final SchedulingSettings schedulingSettings;

    BookingSchedules(ProfessionalCatalog professionalCatalog, SchedulingSettings schedulingSettings) {
        this.professionalCatalog = professionalCatalog;
        this.schedulingSettings = schedulingSettings;
    }

    ProfessionalSummary activeProfessional(UUID professionalId) {
        return professionalCatalog.findById(professionalId)
                .filter(ProfessionalSummary::active)
                .orElseThrow(() -> new ResourceNotFoundException("El profesional seleccionado no existe."));
    }

    ProfessionalSchedule scheduleOf(UUID professionalId) {
        return schedulingSettings.availabilityOf(professionalId)
                .map(BookingSchedules::toSchedule)
                .orElseThrow(() -> new BusinessRuleViolationException("professionalId",
                        "Este profesional aún no tiene horarios para agendar. Elige otro."));
    }

    boolean hasSchedule(UUID professionalId) {
        return schedulingSettings.availabilityOf(professionalId).isPresent();
    }

    BookingPeriod periodStarting(LocalDate today) {
        return new BookingPeriod(today, schedulingSettings.lastBookableDate(today));
    }

    private static ProfessionalSchedule toSchedule(AvailabilitySummary availability) {
        return new ProfessionalSchedule(availability.workingDays(), availability.slotStartTimes(),
                availability.slotMinutes());
    }
}
