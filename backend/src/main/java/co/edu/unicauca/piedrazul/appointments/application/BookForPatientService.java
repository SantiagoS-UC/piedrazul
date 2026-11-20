package co.edu.unicauca.piedrazul.appointments.application;

import co.edu.unicauca.piedrazul.appointments.domain.PatientAlreadyBookedException;
import co.edu.unicauca.piedrazul.identity.PatientDirectory;
import co.edu.unicauca.piedrazul.identity.PatientSummary;
import co.edu.unicauca.piedrazul.shared.domain.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * El agendador registra la cita de un paciente que la pide por teléfono o WhatsApp. Las reglas son
 * las mismas que cuando el paciente agenda por su cuenta, por eso se delega en BookAppointmentService.
 */
@Service
public class BookForPatientService {

    private final PatientDirectory patientDirectory;
    private final BookAppointmentService bookAppointmentService;

    BookForPatientService(PatientDirectory patientDirectory, BookAppointmentService bookAppointmentService) {
        this.patientDirectory = patientDirectory;
        this.bookAppointmentService = bookAppointmentService;
    }

    public StaffBookedAppointment book(BookAppointmentCommand command) {
        PatientSummary patient = patientDirectory.findByIds(List.of(command.patientId())).stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No encontramos al paciente seleccionado."));
        try {
            return new StaffBookedAppointment(bookAppointmentService.book(command), patient);
        } catch (PatientAlreadyBookedException ex) {
            // El mensaje original le habla al paciente; aquí lo lee el agendador.
            throw PatientAlreadyBookedException.forStaff();
        }
    }
}
