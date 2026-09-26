package co.edu.unicauca.piedrazul.identity.application;

import co.edu.unicauca.piedrazul.identity.PatientDirectory;
import co.edu.unicauca.piedrazul.identity.PatientSummary;
import co.edu.unicauca.piedrazul.identity.domain.Patient;
import co.edu.unicauca.piedrazul.identity.domain.PatientRepository;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientDirectoryService implements PatientDirectory {

    private final PatientRepository patientRepository;

    public PatientDirectoryService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientSummary> findByIds(Collection<UUID> patientIds) {
        if (patientIds == null || patientIds.isEmpty()) {
            return List.of();
        }
        return patientRepository.findAllById(patientIds).stream()
                .map(PatientDirectoryService::toSummary)
                .toList();
    }

    private static PatientSummary toSummary(Patient patient) {
        return new PatientSummary(
                patient.id(),
                patient.name().firstName(),
                patient.name().middleName(),
                patient.name().firstLastName(),
                patient.name().secondLastName(),
                patient.document().type().name(),
                patient.document().number(),
                patient.phone().value());
    }
}
