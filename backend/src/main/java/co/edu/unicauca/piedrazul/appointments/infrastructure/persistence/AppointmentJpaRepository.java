package co.edu.unicauca.piedrazul.appointments.infrastructure.persistence;

import co.edu.unicauca.piedrazul.appointments.domain.AppointmentStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface AppointmentJpaRepository extends JpaRepository<AppointmentEntity, UUID> {

    List<AppointmentEntity> findByProfessionalIdAndAppointmentDateAndStatusOrderByStartTime(UUID professionalId,
            LocalDate appointmentDate, AppointmentStatus status);

    List<AppointmentEntity> findByPatientIdAndAppointmentDateAndStatusOrderByStartTime(UUID patientId,
            LocalDate appointmentDate, AppointmentStatus status);

    List<AppointmentEntity> findByPatientIdOrderByAppointmentDateDescStartTimeDesc(UUID patientId);
}
