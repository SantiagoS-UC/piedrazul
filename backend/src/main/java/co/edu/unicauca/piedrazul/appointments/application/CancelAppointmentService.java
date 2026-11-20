package co.edu.unicauca.piedrazul.appointments.application;

import co.edu.unicauca.piedrazul.appointments.domain.Appointment;
import co.edu.unicauca.piedrazul.appointments.domain.AppointmentRepository;
import co.edu.unicauca.piedrazul.professionals.ProfessionalCatalog;
import co.edu.unicauca.piedrazul.shared.domain.AccessForbiddenException;
import co.edu.unicauca.piedrazul.shared.domain.ResourceNotFoundException;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * RF6: el paciente cancela una cita propia con mínimo dos días de anticipación y dejando el motivo.
 * Las reglas están en Appointment; aquí se valida que la cita sea suya.
 */
@Service
public class CancelAppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ProfessionalCatalog professionalCatalog;
    private final Clock clock;

    CancelAppointmentService(AppointmentRepository appointmentRepository, ProfessionalCatalog professionalCatalog,
            Clock clock) {
        this.appointmentRepository = appointmentRepository;
        this.professionalCatalog = professionalCatalog;
        this.clock = clock;
    }

    @Transactional
    public PatientHistoryEntry cancel(CancelAppointmentCommand command) {
        Appointment appointment = appointmentRepository.findById(command.appointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("La cita que quieres cancelar no existe."));
        if (!appointment.patientId().equals(command.patientId())) {
            throw new AccessForbiddenException("Solo puedes cancelar tus propias citas.");
        }
        Appointment cancelled = appointment.cancel(command.reason(), clock.instant(), LocalDate.now(clock));
        appointmentRepository.save(cancelled);
        return new PatientHistoryEntry(cancelled, professionalCatalog.findById(cancelled.professionalId()), false,
                false);
    }
}
