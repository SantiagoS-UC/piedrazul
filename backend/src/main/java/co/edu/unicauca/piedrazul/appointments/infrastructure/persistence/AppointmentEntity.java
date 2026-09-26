package co.edu.unicauca.piedrazul.appointments.infrastructure.persistence;

import co.edu.unicauca.piedrazul.appointments.domain.Appointment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "appointments")
class AppointmentEntity {

    @Id
    private UUID id;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "professional_id", nullable = false)
    private UUID professionalId;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AppointmentEntity() {
        // Requerido por JPA.
    }

    static AppointmentEntity fromDomain(Appointment appointment) {
        AppointmentEntity entity = new AppointmentEntity();
        entity.id = appointment.id();
        entity.patientId = appointment.patientId();
        entity.professionalId = appointment.professionalId();
        entity.appointmentDate = appointment.date();
        entity.startTime = appointment.startTime();
        entity.durationMinutes = appointment.durationMinutes();
        entity.createdAt = appointment.createdAt();
        return entity;
    }

    Appointment toDomain() {
        return Appointment.restore(id, patientId, professionalId, appointmentDate, startTime, durationMinutes,
                createdAt);
    }
}
