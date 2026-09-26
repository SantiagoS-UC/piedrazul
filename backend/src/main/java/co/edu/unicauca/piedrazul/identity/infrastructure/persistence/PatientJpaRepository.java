package co.edu.unicauca.piedrazul.identity.infrastructure.persistence;

import co.edu.unicauca.piedrazul.identity.domain.DocumentType;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PatientJpaRepository extends JpaRepository<PatientEntity, UUID> {

    boolean existsByDocumentTypeAndDocumentNumber(DocumentType documentType, String documentNumber);
}
