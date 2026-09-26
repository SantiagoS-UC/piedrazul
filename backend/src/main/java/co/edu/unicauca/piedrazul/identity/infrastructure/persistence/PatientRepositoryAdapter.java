package co.edu.unicauca.piedrazul.identity.infrastructure.persistence;

import co.edu.unicauca.piedrazul.identity.domain.IdentityDocument;
import co.edu.unicauca.piedrazul.identity.domain.Patient;
import co.edu.unicauca.piedrazul.identity.domain.PatientRepository;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class PatientRepositoryAdapter implements PatientRepository {

    private final PatientJpaRepository jpaRepository;

    PatientRepositoryAdapter(PatientJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean existsByDocument(IdentityDocument document) {
        return jpaRepository.existsByDocumentTypeAndDocumentNumber(document.type(), document.number());
    }

    @Override
    public void save(Patient patient) {
        jpaRepository.save(PatientEntity.fromDomain(patient));
    }

    @Override
    public List<Patient> findAllById(Collection<UUID> ids) {
        return jpaRepository.findAllById(ids).stream().map(PatientEntity::toDomain).toList();
    }
}
