package co.edu.unicauca.piedrazul.appointments.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository {

    /**
     * Registra la cita, o la actualiza si ya existe una con el mismo id (por ejemplo al reagendarla).
     *
     * @throws SlotTakenException             si otra persona reservó la franja al mismo tiempo
     * @throws PatientAlreadyBookedException  si el paciente ya tiene una cita a esa hora
     */
    void save(Appointment appointment);

    Optional<Appointment> findById(UUID appointmentId);

    List<Appointment> findByProfessionalAndDate(UUID professionalId, LocalDate date);

    List<Appointment> findByPatientAndDate(UUID patientId, LocalDate date);

    /**
     * Todas las citas del paciente, de la más reciente a la más antigua.
     */
    List<Appointment> findByPatient(UUID patientId);
}
