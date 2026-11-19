package co.edu.unicauca.piedrazul.configuration.application;

import co.edu.unicauca.piedrazul.configuration.AvailabilitySummary;
import co.edu.unicauca.piedrazul.configuration.SchedulingSettings;
import co.edu.unicauca.piedrazul.configuration.domain.ProfessionalAvailability;
import co.edu.unicauca.piedrazul.configuration.domain.ProfessionalAvailabilityRepository;
import co.edu.unicauca.piedrazul.configuration.domain.SchedulingWindowRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SchedulingSettingsService implements SchedulingSettings {

    private final SchedulingWindowRepository windowRepository;
    private final ProfessionalAvailabilityRepository availabilityRepository;

    public SchedulingSettingsService(SchedulingWindowRepository windowRepository,
            ProfessionalAvailabilityRepository availabilityRepository) {
        this.windowRepository = windowRepository;
        this.availabilityRepository = availabilityRepository;
    }

    @Override
    public int schedulingWindowWeeks() {
        return windowRepository.current().weeks();
    }

    @Override
    public LocalDate lastBookableDate(LocalDate today) {
        return windowRepository.current().lastBookableDate(today);
    }

    @Override
    public Optional<AvailabilitySummary> availabilityOf(UUID professionalId) {
        return availabilityRepository.findByProfessionalId(professionalId)
                .map(SchedulingSettingsService::toSummary);
    }

    static AvailabilitySummary toSummary(ProfessionalAvailability availability) {
        return new AvailabilitySummary(
                availability.professionalId(),
                availability.workingDays(),
                availability.startTime(),
                availability.endTime(),
                availability.slotMinutes(),
                availability.slotStartTimes());
    }
}
