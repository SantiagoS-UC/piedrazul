package co.edu.unicauca.piedrazul.configuration.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ProfessionalAvailabilityJpaRepository extends JpaRepository<ProfessionalAvailabilityEntity, UUID> {
}
