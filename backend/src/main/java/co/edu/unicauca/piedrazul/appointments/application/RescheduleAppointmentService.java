package co.edu.unicauca.piedrazul.appointments.application;

import co.edu.unicauca.piedrazul.appointments.domain.Appointment;
import co.edu.unicauca.piedrazul.appointments.domain.AppointmentRepository;
import co.edu.unicauca.piedrazul.appointments.domain.PatientAlreadyBookedException;
import co.edu.unicauca.piedrazul.appointments.domain.ProfessionalSchedule;
import co.edu.unicauca.piedrazul.appointments.domain.SlotTakenException;
import co.edu.unicauca.piedrazul.professionals.ProfessionalSummary;
import co.edu.unicauca.piedrazul.shared.domain.AccessForbiddenException;
import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import co.edu.unicauca.piedrazul.shared.domain.ResourceNotFoundException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * El profesional mueve una cita de su agenda a otra fecha u hora, por ejemplo si no puede atender
 * ese día. El agendador también puede hacerlo con la agenda de cualquier profesional. Se aplican las
 * mismas reglas que al agendar.
 */
@Service
public class RescheduleAppointmentService {

    private final BookingSchedules schedules;
    private final AppointmentRepository appointmentRepository;
    private final Clock clock;

    RescheduleAppointmentService(BookingSchedules schedules, AppointmentRepository appointmentRepository,
            Clock clock) {
        this.schedules = schedules;
        this.appointmentRepository = appointmentRepository;
        this.clock = clock;
    }

    @Transactional
    public BookedAppointment reschedule(Requester requester, RescheduleAppointmentCommand command) {
        Appointment appointment = appointmentRepository.findById(command.appointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("La cita que quieres reagendar no existe."));
        if (requester.professional() && !requester.userId().equals(appointment.professionalId())) {
            throw new AccessForbiddenException("Solo puedes reagendar citas de tu propia agenda.");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        if (!appointment.isUpcomingAt(now)) {
            throw new BusinessRuleViolationException("Solo se pueden reagendar citas que aún no han empezado.");
        }
        LocalDate date = command.date();
        LocalTime time = command.startTime();
        if (appointment.startsAt(date, time)) {
            throw new BusinessRuleViolationException("time",
                    "La cita ya está en ese horario. Elige otra fecha u hora.");
        }

        ProfessionalSummary professional = schedules.activeProfessional(appointment.professionalId());
        ProfessionalSchedule schedule = schedules.scheduleOf(appointment.professionalId());
        SlotRules.checkOffered(schedule, schedules.periodStarting(now.toLocalDate()), date, time, now);
        int minutes = schedule.slotMinutes();
        if (SlotRules.anyOverlaps(appointmentRepository.findByProfessionalAndDate(professional.id(), date), date,
                time, minutes, appointment.id())) {
            throw new SlotTakenException();
        }
        if (SlotRules.anyOverlaps(appointmentRepository.findByPatientAndDate(appointment.patientId(), date), date,
                time, minutes, appointment.id())) {
            throw PatientAlreadyBookedException.forStaff();
        }

        Appointment rescheduled = appointment.rescheduleTo(date, time, minutes);
        appointmentRepository.save(rescheduled);
        return new BookedAppointment(rescheduled, professional);
    }
}
