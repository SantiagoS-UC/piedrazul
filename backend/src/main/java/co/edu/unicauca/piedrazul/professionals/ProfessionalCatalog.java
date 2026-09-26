package co.edu.unicauca.piedrazul.professionals;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Consulta de médicos y terapistas para otros módulos (configuración y citas).
 */
public interface ProfessionalCatalog {

    Optional<ProfessionalSummary> findById(UUID professionalId);

    /**
     * Todos los profesionales, ordenados alfabéticamente por apellido y luego por nombre.
     */
    List<ProfessionalSummary> findAll();
}
