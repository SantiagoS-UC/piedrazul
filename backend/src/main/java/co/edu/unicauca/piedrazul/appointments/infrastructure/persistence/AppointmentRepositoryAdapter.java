package co.edu.unicauca.piedrazul.appointments.infrastructure.persistence;

import co.edu.unicauca.piedrazul.appointments.domain.Appointment;
import co.edu.unicauca.piedrazul.appointments.domain.AppointmentRepository;
import co.edu.unicauca.piedrazul.appointments.domain.AppointmentStatus;
import co.edu.unicauca.piedrazul.appointments.domain.PatientAlreadyBookedException;
import co.edu.unicauca.piedrazul.appointments.domain.SlotTakenException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
class AppointmentRepositoryAdapter implements AppointmentRepository {

    // Nombres de los índices únicos de V4_2__add_appointment_cancellation.sql (antes restricciones de V4_1).
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
    public Optional<Appointment> findById(UUID appointmentId) {
        return jpaRepository.findById(appointmentId).map(AppointmentEntity::toDomain);
    }

    @Override
    public List<Appointment> findByProfessionalAndDate(UUID professionalId, LocalDate date) {
        return jpaRepository
                .findByProfessionalIdAndAppointmentDateAndStatusOrderByStartTime(professionalId, date,
                        AppointmentStatus.SCHEDULED)
                .stream()
                .map(AppointmentEntity::toDomain)
                .toList();
    }

    @Override
    public List<Appointment> findByPatient(UUID patientId) {
        return jpaRepository.findByPatientIdOrderByAppointmentDateDescStartTimeDesc(patientId).stream()
                .map(AppointmentEntity::toDomain)
                .toList();
    }

    @Override
    public List<Appointment> findByPatientAndDate(UUID patientId, LocalDate date) {
        return jpaRepository
                .findByPatientIdAndAppointmentDateAndStatusOrderByStartTime(patientId, date,
                        AppointmentStatus.SCHEDULED)
                .stream()
                .map(AppointmentEntity::toDomain)
                .toList();
    }
}
