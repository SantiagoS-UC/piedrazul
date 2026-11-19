package co.edu.unicauca.piedrazul.professionals.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ProfessionalJpaRepository extends JpaRepository<ProfessionalEntity, UUID> {
}
