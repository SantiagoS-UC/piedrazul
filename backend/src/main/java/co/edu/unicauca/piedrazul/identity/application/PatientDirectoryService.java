package co.edu.unicauca.piedrazul.identity.application;

import co.edu.unicauca.piedrazul.identity.PatientDirectory;
import co.edu.unicauca.piedrazul.identity.PatientSummary;
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
                .map(PatientSummaries::of)
                .toList();
    }
}
