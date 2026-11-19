package co.edu.unicauca.piedrazul.appointments.application;

import co.edu.unicauca.piedrazul.appointments.domain.Appointment;
import co.edu.unicauca.piedrazul.appointments.domain.AppointmentRepository;
import co.edu.unicauca.piedrazul.appointments.domain.PatientAlreadyBookedException;
import co.edu.unicauca.piedrazul.appointments.domain.ProfessionalSchedule;
import co.edu.unicauca.piedrazul.appointments.domain.SlotTakenException;
import co.edu.unicauca.piedrazul.professionals.ProfessionalSummary;
import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-03, paso 4: registra la cita después de revalidar todo, porque entre que el paciente vio las
 * franjas y confirmó pudo cambiar la configuración u otra persona pudo tomar el horario.
 */
@Service
public class BookAppointmentService {

    private final BookingSchedules schedules;
    private final AppointmentRepository appointmentRepository;
    private final Clock clock;

    BookAppointmentService(BookingSchedules schedules, AppointmentRepository appointmentRepository, Clock clock) {
        this.schedules = schedules;
        this.appointmentRepository = appointmentRepository;
        this.clock = clock;
    }

    @Transactional
    public BookedAppointment book(BookAppointmentCommand command) {
        ProfessionalSummary professional = schedules.activeProfessional(command.professionalId());
        ProfessionalSchedule schedule = schedules.scheduleOf(command.professionalId());
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDate date = command.date();

        if (!schedules.periodStarting(now.toLocalDate()).includes(date) || !schedule.worksOn(date)) {
            throw new BusinessRuleViolationException("date", "Elige una fecha disponible del calendario.");
        }
        if (!schedule.offersSlotAt(command.startTime())) {
            throw new BusinessRuleViolationException("time", "Elige una de las horas disponibles.");
        }
        if (!date.atTime(command.startTime()).isAfter(now)) {
            throw new BusinessRuleViolationException("time", "Esa hora ya pasó. Elige una hora más tarde.");
        }
        // Primero la cita propia: si el paciente ya reservó esta misma franja, ese mensaje le sirve más.
        boolean patientBusy = appointmentRepository.findByPatientAndDate(command.patientId(), date).stream()
                .anyMatch(existing -> existing.overlaps(date, command.startTime(), schedule.slotMinutes()));
        if (patientBusy) {
            throw new PatientAlreadyBookedException();
        }
        boolean slotTaken = appointmentRepository.findByProfessionalAndDate(command.professionalId(), date).stream()
                .anyMatch(existing -> existing.overlaps(date, command.startTime(), schedule.slotMinutes()));
        if (slotTaken) {
            throw new SlotTakenException();
        }

        Appointment appointment = Appointment.book(UUID.randomUUID(), command.patientId(), command.professionalId(),
                date, command.startTime(), schedule.slotMinutes(), clock.instant());
        appointmentRepository.save(appointment);
        return new BookedAppointment(appointment, professional);
    }
}
