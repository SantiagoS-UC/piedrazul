package co.edu.unicauca.piedrazul.configuration;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Parámetros de agendamiento para otros módulos, principalmente el de citas.
 */
public interface SchedulingSettings {

    /**
     * Cuántas semanas hacia adelante se pueden agendar citas.
     */
    int schedulingWindowWeeks();

    /**
     * Último día que un paciente puede elegir si agenda hoy.
     */
    LocalDate lastBookableDate(LocalDate today);

    /**
     * Vacío si el administrador aún no configuró la disponibilidad de ese profesional.
     */
    Optional<AvailabilitySummary> availabilityOf(UUID professionalId);
}
