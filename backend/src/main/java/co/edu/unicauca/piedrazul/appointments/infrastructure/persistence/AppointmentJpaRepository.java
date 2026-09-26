package co.edu.unicauca.piedrazul.appointments.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface AppointmentJpaRepository extends JpaRepository<AppointmentEntity, UUID> {

    List<AppointmentEntity> findByProfessionalIdAndAppointmentDateOrderByStartTime(UUID professionalId,
            LocalDate appointmentDate);

    List<AppointmentEntity> findByPatientIdAndAppointmentDateOrderByStartTime(UUID patientId,
            LocalDate appointmentDate);
}
