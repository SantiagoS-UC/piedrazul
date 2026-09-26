package co.edu.unicauca.piedrazul.configuration.application;

import static java.time.DayOfWeek.MONDAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import co.edu.unicauca.piedrazul.configuration.AvailabilitySummary;
import co.edu.unicauca.piedrazul.configuration.domain.ProfessionalAvailability;
import co.edu.unicauca.piedrazul.configuration.domain.ProfessionalAvailabilityRepository;
import co.edu.unicauca.piedrazul.configuration.domain.SchedulingWindow;
import co.edu.unicauca.piedrazul.configuration.domain.SchedulingWindowRepository;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SchedulingSettingsServiceTest {

    @Mock
    private SchedulingWindowRepository windowRepository;
    @Mock
    private ProfessionalAvailabilityRepository availabilityRepository;

    private SchedulingSettingsService service;

    @BeforeEach
    void setUp() {
        service = new SchedulingSettingsService(windowRepository, availabilityRepository);
    }

    @Test
    @DisplayName("Expone el número de semanas de la ventana vigente")
    void exposesWindowWeeks() {
        when(windowRepository.current()).thenReturn(new SchedulingWindow(3));

        assertThat(service.schedulingWindowWeeks()).isEqualTo(3);
    }

    @Test
    @DisplayName("Expone la disponibilidad con sus franjas ya calculadas")
    void exposesAvailabilityWithSlots() {
        UUID id = UUID.randomUUID();
        when(availabilityRepository.findByProfessionalId(id)).thenReturn(Optional.of(
                new ProfessionalAvailability(id, Set.of(MONDAY), LocalTime.of(8, 0), LocalTime.of(9, 0), 20)));

        AvailabilitySummary summary = service.availabilityOf(id).orElseThrow();

        assertThat(summary.worksOn(MONDAY)).isTrue();
        assertThat(summary.slotStartTimes())
                .containsExactly(LocalTime.of(8, 0), LocalTime.of(8, 20), LocalTime.of(8, 40));
    }

    @Test
    @DisplayName("Responde vacío si el profesional no tiene disponibilidad")
    void emptyWhenNotConfigured() {
        UUID id = UUID.randomUUID();
        when(availabilityRepository.findByProfessionalId(id)).thenReturn(Optional.empty());

        assertThat(service.availabilityOf(id)).isEmpty();
    }
}
