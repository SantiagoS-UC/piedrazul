package co.edu.unicauca.piedrazul.configuration.infrastructure.persistence;

import co.edu.unicauca.piedrazul.configuration.domain.ProfessionalAvailability;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "professional_availabilities")
class ProfessionalAvailabilityEntity {

    @Id
    @Column(name = "professional_id")
    private UUID professionalId;

    // Días separados por comas, por ejemplo "MONDAY,WEDNESDAY". Son a lo sumo seis valores fijos,
    // así que no justifican una tabla aparte.
    @Column(name = "working_days", nullable = false)
    private String workingDays;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "slot_minutes", nullable = false)
    private Integer slotMinutes;

    protected ProfessionalAvailabilityEntity() {
        // Requerido por JPA.
    }

    static ProfessionalAvailabilityEntity fromDomain(ProfessionalAvailability availability) {
        ProfessionalAvailabilityEntity entity = new ProfessionalAvailabilityEntity();
        entity.professionalId = availability.professionalId();
        entity.workingDays = availability.workingDays().stream()
                .map(DayOfWeek::name)
                .collect(Collectors.joining(","));
        entity.startTime = availability.startTime();
        entity.endTime = availability.endTime();
        entity.slotMinutes = availability.slotMinutes();
        return entity;
    }

    ProfessionalAvailability toDomain() {
        Set<DayOfWeek> days = Arrays.stream(workingDays.split(","))
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(DayOfWeek.class)));
        return new ProfessionalAvailability(professionalId, days, startTime, endTime, slotMinutes);
    }
}
