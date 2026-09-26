package co.edu.unicauca.piedrazul.configuration.application;

import co.edu.unicauca.piedrazul.configuration.domain.ProfessionalAvailability;
import co.edu.unicauca.piedrazul.configuration.domain.ProfessionalAvailabilityRepository;
import co.edu.unicauca.piedrazul.professionals.ProfessionalCatalog;
import co.edu.unicauca.piedrazul.shared.domain.ResourceNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-06: días de atención, franja horaria e intervalo entre citas de cada profesional.
 */
@Service
public class AvailabilityService {

    private final ProfessionalAvailabilityRepository repository;
    private final ProfessionalCatalog professionalCatalog;

    public AvailabilityService(ProfessionalAvailabilityRepository repository,
            ProfessionalCatalog professionalCatalog) {
        this.repository = repository;
        this.professionalCatalog = professionalCatalog;
    }

    @Transactional(readOnly = true)
    public Optional<ProfessionalAvailability> findByProfessional(UUID professionalId) {
        return repository.findByProfessionalId(professionalId);
    }

    /**
     * Crea o reemplaza la disponibilidad. Las citas ya agendadas no se modifican.
     */
    @Transactional
    public ProfessionalAvailability save(SaveAvailabilityCommand command) {
        if (professionalCatalog.findById(command.professionalId()).isEmpty()) {
            throw new ResourceNotFoundException("El profesional seleccionado no existe.");
        }
        ProfessionalAvailability availability = new ProfessionalAvailability(command.professionalId(),
                command.workingDays(), command.startTime(), command.endTime(), command.slotMinutes());
        repository.save(availability);
        return availability;
    }
}
