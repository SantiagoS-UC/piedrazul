package co.edu.unicauca.piedrazul.configuration;

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
     * Vacío si el administrador aún no configuró la disponibilidad de ese profesional.
     */
    Optional<AvailabilitySummary> availabilityOf(UUID professionalId);
}
