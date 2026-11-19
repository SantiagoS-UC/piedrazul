package co.edu.unicauca.piedrazul.professionals.infrastructure.persistence;

import co.edu.unicauca.piedrazul.professionals.domain.Professional;
import co.edu.unicauca.piedrazul.professionals.domain.ProfessionalRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class ProfessionalRepositoryAdapter implements ProfessionalRepository {

    private final ProfessionalJpaRepository jpaRepository;

    ProfessionalRepositoryAdapter(ProfessionalJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Professional> findById(UUID id) {
        return jpaRepository.findById(id).map(ProfessionalEntity::toDomain);
    }

    @Override
    public List<Professional> findAll() {
        return jpaRepository.findAll().stream().map(ProfessionalEntity::toDomain).toList();
    }
}
