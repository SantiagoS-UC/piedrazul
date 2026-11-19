package co.edu.unicauca.piedrazul.identity.domain;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PatientRepository {

    boolean existsByDocument(IdentityDocument document);

    void save(Patient patient);

    List<Patient> findAllById(Collection<UUID> ids);

    /**
     * Pacientes cuyo número de documento empieza por {@code prefix}, ordenados por ese número.
     */
    List<Patient> findByDocumentNumberStartingWith(String prefix, int limit);
}
