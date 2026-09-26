package co.edu.unicauca.piedrazul.appointments.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository {

    /**
     * @throws SlotTakenException             si otra persona reservó la franja al mismo tiempo
     * @throws PatientAlreadyBookedException  si el paciente ya tiene una cita a esa hora
     */
    void save(Appointment appointment);

    List<Appointment> findByProfessionalAndDate(UUID professionalId, LocalDate date);

    List<Appointment> findByPatientAndDate(UUID patientId, LocalDate date);
}
