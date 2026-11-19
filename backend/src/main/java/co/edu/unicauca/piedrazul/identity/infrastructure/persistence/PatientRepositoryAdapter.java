package co.edu.unicauca.piedrazul.identity.infrastructure.persistence;

import co.edu.unicauca.piedrazul.identity.domain.IdentityDocument;
import co.edu.unicauca.piedrazul.identity.domain.Patient;
import co.edu.unicauca.piedrazul.identity.domain.PatientRepository;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
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
        // saveAndFlush para que un error de la base de datos aparezca aquí y el registro pueda
        // borrar la cuenta que ya se creó en Keycloak.
        jpaRepository.saveAndFlush(PatientEntity.fromDomain(patient));
    }

    @Override
    public List<Patient> findAllById(Collection<UUID> ids) {
        return jpaRepository.findAllById(ids).stream().map(PatientEntity::toDomain).toList();
    }

    @Override
    public List<Patient> findByDocumentNumberStartingWith(String prefix, int limit) {
        return jpaRepository.findByDocumentNumberStartingWithOrderByDocumentNumber(prefix, Limit.of(limit)).stream()
                .map(PatientEntity::toDomain)
                .toList();
    }
}
