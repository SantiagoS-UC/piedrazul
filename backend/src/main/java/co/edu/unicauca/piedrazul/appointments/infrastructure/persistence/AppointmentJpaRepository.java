package co.edu.unicauca.piedrazul.appointments.infrastructure.persistence;

import co.edu.unicauca.piedrazul.appointments.domain.AppointmentStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface AppointmentJpaRepository extends JpaRepository<AppointmentEntity, UUID> {

    List<AppointmentEntity> findByProfessionalIdAndAppointmentDateAndStatusOrderByStartTime(UUID professionalId,
            LocalDate appointmentDate, AppointmentStatus status);

    List<AppointmentEntity> findByPatientIdAndAppointmentDateAndStatusOrderByStartTime(UUID patientId,
            LocalDate appointmentDate, AppointmentStatus status);

    List<AppointmentEntity> findByPatientIdOrderByAppointmentDateDescStartTimeDesc(UUID patientId);

    List<AppointmentEntity> findByAppointmentDateBetweenAndStatus(LocalDate from, LocalDate to,
            AppointmentStatus status);

    Optional<AppointmentEntity> findFirstByStatusOrderByAppointmentDateAsc(AppointmentStatus status);

    Optional<AppointmentEntity> findFirstByStatusOrderByAppointmentDateDesc(AppointmentStatus status);
}
