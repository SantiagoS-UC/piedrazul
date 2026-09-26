package co.edu.unicauca.piedrazul.appointments.application;

import co.edu.unicauca.piedrazul.appointments.domain.Appointment;
import co.edu.unicauca.piedrazul.appointments.domain.AppointmentRepository;
import co.edu.unicauca.piedrazul.identity.PatientDirectory;
import co.edu.unicauca.piedrazul.identity.PatientSummary;
import co.edu.unicauca.piedrazul.professionals.ProfessionalCatalog;
import co.edu.unicauca.piedrazul.professionals.ProfessionalSummary;
import co.edu.unicauca.piedrazul.shared.domain.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-04: citas de un profesional en una fecha, con los datos del paciente para el agendador.
 */
@Service
@Transactional(readOnly = true)
public class AgendaQueryService {

    private final ProfessionalCatalog professionalCatalog;
    private final PatientDirectory patientDirectory;
    private final AppointmentRepository appointmentRepository;

    AgendaQueryService(ProfessionalCatalog professionalCatalog, PatientDirectory patientDirectory,
            AppointmentRepository appointmentRepository) {
        this.professionalCatalog = professionalCatalog;
        this.patientDirectory = patientDirectory;
        this.appointmentRepository = appointmentRepository;
    }

    // Incluye profesionales inactivos: el agendador puede necesitar consultar citas que ya tenían.
    public ProfessionalAgenda agendaOf(UUID professionalId, LocalDate date) {
        ProfessionalSummary professional = professionalCatalog.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("El profesional seleccionado no existe."));
        List<Appointment> appointments = appointmentRepository.findByProfessionalAndDate(professionalId, date)
                .stream()
                .sorted(Comparator.comparing(Appointment::startTime))
                .toList();

        // Una sola consulta al módulo identity para todos los pacientes del día.
        Map<UUID, PatientSummary> patients = patientDirectory
                .findByIds(appointments.stream().map(Appointment::patientId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(PatientSummary::id, Function.identity()));

        List<AgendaEntry> entries = appointments.stream()
                .map(appointment -> new AgendaEntry(appointment,
                        Optional.ofNullable(patients.get(appointment.patientId()))))
                .toList();
        return new ProfessionalAgenda(professional, date, entries);
    }
}
