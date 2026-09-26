package co.edu.unicauca.piedrazul.configuration.domain;

import java.util.Optional;
import java.util.UUID;

public interface ProfessionalAvailabilityRepository {

    Optional<ProfessionalAvailability> findByProfessionalId(UUID professionalId);

    void save(ProfessionalAvailability availability);
}
