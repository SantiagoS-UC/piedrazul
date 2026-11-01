package co.edu.unicauca.piedrazul.identity.application;

import co.edu.unicauca.piedrazul.identity.PatientSummary;
import co.edu.unicauca.piedrazul.identity.domain.PatientRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * RF5: sugerencias de pacientes mientras el agendador o el profesional escriben la cédula.
 */
@Service
public class PatientSearchService {

    // Con menos dígitos la lista sería casi todo el registro de pacientes.
    static final int MIN_PREFIX_LENGTH = 3;
    static final int MAX_RESULTS = 8;

    private final PatientRepository patientRepository;

    public PatientSearchService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional(readOnly = true)
    public List<PatientSummary> searchByDocument(String documentPrefix) {
        String prefix = documentPrefix == null ? "" : documentPrefix.strip().toUpperCase(Locale.ROOT);
        if (prefix.length() < MIN_PREFIX_LENGTH) {
            return List.of();
        }
        return patientRepository.findByDocumentNumberStartingWith(prefix, MAX_RESULTS).stream()
                .map(PatientSummaries::of)
                .toList();
    }
}
