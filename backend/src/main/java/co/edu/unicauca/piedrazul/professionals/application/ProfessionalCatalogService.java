package co.edu.unicauca.piedrazul.professionals.application;

import co.edu.unicauca.piedrazul.professionals.ProfessionalCatalog;
import co.edu.unicauca.piedrazul.professionals.ProfessionalSummary;
import co.edu.unicauca.piedrazul.professionals.domain.Professional;
import co.edu.unicauca.piedrazul.professionals.domain.ProfessionalRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProfessionalCatalogService implements ProfessionalCatalog {

    private final ProfessionalRepository professionalRepository;

    public ProfessionalCatalogService(ProfessionalRepository professionalRepository) {
        this.professionalRepository = professionalRepository;
    }

    @Override
    public Optional<ProfessionalSummary> findById(UUID professionalId) {
        return professionalRepository.findById(professionalId).map(ProfessionalCatalogService::toSummary);
    }

    @Override
    public List<ProfessionalSummary> findAll() {
        return professionalRepository.findAll().stream()
                .sorted(Professional.ALPHABETICAL)
                .map(ProfessionalCatalogService::toSummary)
                .toList();
    }

    private static ProfessionalSummary toSummary(Professional professional) {
        return new ProfessionalSummary(
                professional.id(),
                professional.firstName(),
                professional.lastName(),
                professional.specialty().name(),
                professional.specialty().displayName(),
                professional.active());
    }
}
