package co.edu.unicauca.piedrazul.appointments.application;

import co.edu.unicauca.piedrazul.appointments.domain.Appointment;
import co.edu.unicauca.piedrazul.appointments.domain.AppointmentRepository;
import co.edu.unicauca.piedrazul.identity.PatientDirectory;
import co.edu.unicauca.piedrazul.identity.PatientSummary;
import co.edu.unicauca.piedrazul.professionals.ProfessionalCatalog;
import co.edu.unicauca.piedrazul.professionals.ProfessionalSummary;
import co.edu.unicauca.piedrazul.shared.domain.ResourceNotFoundException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * RF5: historial de citas de un paciente con todos los profesionales, para el agendador y para
 * cualquier médico o terapista.
 */
@Service
@Transactional(readOnly = true)
public class PatientHistoryQueryService {

    private final PatientDirectory patientDirectory;
    private final ProfessionalCatalog professionalCatalog;
    private final AppointmentRepository appointmentRepository;
    private final Clock clock;

    PatientHistoryQueryService(PatientDirectory patientDirectory, ProfessionalCatalog professionalCatalog,
            AppointmentRepository appointmentRepository, Clock clock) {
        this.patientDirectory = patientDirectory;
        this.professionalCatalog = professionalCatalog;
        this.appointmentRepository = appointmentRepository;
        this.clock = clock;
    }

    public PatientHistory historyOf(UUID patientId) {
        PatientSummary patient = patientDirectory.findByIds(List.of(patientId)).stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No encontramos al paciente seleccionado."));
        List<Appointment> appointments = appointmentRepository.findByPatient(patientId);

        // El catálogo es pequeño: una sola consulta sale más barata que una por cita.
        Map<UUID, ProfessionalSummary> professionals = professionalCatalog.findAll().stream()
                .collect(Collectors.toMap(ProfessionalSummary::id, Function.identity()));
        LocalDateTime now = LocalDateTime.now(clock);

        List<PatientHistoryEntry> entries = appointments.stream()
                .map(appointment -> new PatientHistoryEntry(appointment,
                        Optional.ofNullable(professionals.get(appointment.professionalId())),
                        appointment.isUpcomingAt(now)))
                .toList();
        return new PatientHistory(patient, entries);
    }
}
