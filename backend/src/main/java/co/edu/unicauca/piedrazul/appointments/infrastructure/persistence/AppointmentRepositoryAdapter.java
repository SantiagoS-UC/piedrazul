package co.edu.unicauca.piedrazul.appointments.infrastructure.persistence;

import co.edu.unicauca.piedrazul.appointments.domain.Appointment;
import co.edu.unicauca.piedrazul.appointments.domain.AppointmentRepository;
import co.edu.unicauca.piedrazul.appointments.domain.PatientAlreadyBookedException;
import co.edu.unicauca.piedrazul.appointments.domain.SlotTakenException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
class AppointmentRepositoryAdapter implements AppointmentRepository {

    // Nombres de las restricciones únicas de V4_1__create_appointments_table.sql.
    static final String PROFESSIONAL_SLOT_CONSTRAINT = "uq_appointments_professional_slot";
    static final String PATIENT_SLOT_CONSTRAINT = "uq_appointments_patient_slot";

    private final AppointmentJpaRepository jpaRepository;

    AppointmentRepositoryAdapter(AppointmentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /**
     * Si dos pacientes confirman la misma franja a la vez, ambos pasan las validaciones del servicio
     * y es la base de datos la que rechaza al segundo. Aquí se traduce ese rechazo al error de negocio.
     */
    @Override
    public void save(Appointment appointment) {
        try {
            jpaRepository.saveAndFlush(AppointmentEntity.fromDomain(appointment));
        } catch (DataIntegrityViolationException ex) {
            String detail = String.valueOf(ex.getMostSpecificCause().getMessage());
            if (detail.contains(PATIENT_SLOT_CONSTRAINT)) {
                throw new PatientAlreadyBookedException();
            }
            if (detail.contains(PROFESSIONAL_SLOT_CONSTRAINT)) {
                throw new SlotTakenException();
            }
            throw ex;
        }
    }

    @Override
    public List<Appointment> findByProfessionalAndDate(UUID professionalId, LocalDate date) {
        return jpaRepository.findByProfessionalIdAndAppointmentDateOrderByStartTime(professionalId, date).stream()
                .map(AppointmentEntity::toDomain)
                .toList();
    }

    @Override
    public List<Appointment> findByPatientAndDate(UUID patientId, LocalDate date) {
        return jpaRepository.findByPatientIdAndAppointmentDateOrderByStartTime(patientId, date).stream()
                .map(AppointmentEntity::toDomain)
                .toList();
    }
}
