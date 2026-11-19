package co.edu.unicauca.piedrazul.appointments.application;

import co.edu.unicauca.piedrazul.appointments.domain.AppointmentRepository;
import co.edu.unicauca.piedrazul.appointments.domain.BookingPeriod;
import co.edu.unicauca.piedrazul.appointments.domain.ProfessionalSchedule;
import co.edu.unicauca.piedrazul.appointments.domain.Slot;
import co.edu.unicauca.piedrazul.professionals.ProfessionalCatalog;
import co.edu.unicauca.piedrazul.professionals.ProfessionalSummary;
import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-03, pasos 2 y 3: profesionales que se pueden agendar, su calendario y las franjas de un día.
 */
@Service
@Transactional(readOnly = true)
public class BookingQueryService {

    private final ProfessionalCatalog professionalCatalog;
    private final BookingSchedules schedules;
    private final AppointmentRepository appointmentRepository;
    private final Clock clock;

    BookingQueryService(ProfessionalCatalog professionalCatalog, BookingSchedules schedules,
            AppointmentRepository appointmentRepository, Clock clock) {
        this.professionalCatalog = professionalCatalog;
        this.schedules = schedules;
        this.appointmentRepository = appointmentRepository;
        this.clock = clock;
    }

    /**
     * Profesionales activos de la especialidad con disponibilidad configurada, en orden alfabético
     * por apellido. Los que no tienen horarios no se muestran porque el paciente no podría agendar.
     */
    public List<ProfessionalSummary> professionalsFor(String specialty) {
        return professionalCatalog.findAll().stream()
                .filter(ProfessionalSummary::active)
                .filter(professional -> professional.specialty().equals(specialty))
                .filter(professional -> schedules.hasSchedule(professional.id()))
                .toList();
    }

    public BookingCalendar calendarOf(UUID professionalId) {
        ProfessionalSummary professional = schedules.activeProfessional(professionalId);
        ProfessionalSchedule schedule = schedules.scheduleOf(professionalId);
        return new BookingCalendar(professional, schedules.periodStarting(LocalDate.now(clock)), schedule);
    }

    public List<Slot> slotsOf(UUID professionalId, LocalDate date) {
        schedules.activeProfessional(professionalId);
        ProfessionalSchedule schedule = schedules.scheduleOf(professionalId);
        LocalDateTime now = LocalDateTime.now(clock);
        BookingPeriod period = schedules.periodStarting(now.toLocalDate());
        if (!period.includes(date) || !schedule.worksOn(date)) {
            throw new BusinessRuleViolationException("date", "Elige una fecha disponible del calendario.");
        }
        return schedule.slotsOn(date, appointmentRepository.findByProfessionalAndDate(professionalId, date), now);
    }
}
