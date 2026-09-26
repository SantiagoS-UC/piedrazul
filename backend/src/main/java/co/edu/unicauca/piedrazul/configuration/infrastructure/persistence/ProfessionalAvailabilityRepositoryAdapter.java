package co.edu.unicauca.piedrazul.configuration.infrastructure.persistence;

import co.edu.unicauca.piedrazul.configuration.domain.ProfessionalAvailability;
import co.edu.unicauca.piedrazul.configuration.domain.ProfessionalAvailabilityRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class ProfessionalAvailabilityRepositoryAdapter implements ProfessionalAvailabilityRepository {

    private final ProfessionalAvailabilityJpaRepository jpaRepository;

    ProfessionalAvailabilityRepositoryAdapter(ProfessionalAvailabilityJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<ProfessionalAvailability> findByProfessionalId(UUID professionalId) {
        return jpaRepository.findById(professionalId).map(ProfessionalAvailabilityEntity::toDomain);
    }

    @Override
    public void save(ProfessionalAvailability availability) {
        jpaRepository.save(ProfessionalAvailabilityEntity.fromDomain(availability));
    }
}
