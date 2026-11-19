package co.edu.unicauca.piedrazul.configuration.application;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.TUESDAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.unicauca.piedrazul.configuration.domain.ProfessionalAvailability;
import co.edu.unicauca.piedrazul.configuration.domain.ProfessionalAvailabilityRepository;
import co.edu.unicauca.piedrazul.professionals.ProfessionalCatalog;
import co.edu.unicauca.piedrazul.professionals.ProfessionalSummary;
import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import co.edu.unicauca.piedrazul.shared.domain.ResourceNotFoundException;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    private static final UUID PROFESSIONAL_ID = UUID.randomUUID();
    private static final ProfessionalSummary PROFESSIONAL = new ProfessionalSummary(PROFESSIONAL_ID, "Laura",
            "Martínez", "GENERAL_MEDICINE", "Medicina general", true);

    @Mock
    private ProfessionalAvailabilityRepository repository;
    @Mock
    private ProfessionalCatalog professionalCatalog;

    private AvailabilityService service;

    @BeforeEach
    void setUp() {
        service = new AvailabilityService(repository, professionalCatalog);
    }

    @Test
    @DisplayName("Guarda la disponibilidad de un profesional existente")
    void savesAvailability() {
        when(professionalCatalog.findById(PROFESSIONAL_ID)).thenReturn(Optional.of(PROFESSIONAL));

        ProfessionalAvailability saved = service.save(command(30));

        ArgumentCaptor<ProfessionalAvailability> captor = ArgumentCaptor.forClass(ProfessionalAvailability.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue()).isSameAs(saved);
        assertThat(saved.workingDays()).containsExactlyInAnyOrder(MONDAY, TUESDAY);
        assertThat(saved.slotStartTimes()).hasSize(8);
    }

    @Test
    @DisplayName("No guarda si el profesional no existe")
    void rejectsUnknownProfessional() {
        when(professionalCatalog.findById(PROFESSIONAL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.save(command(30)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("El profesional seleccionado no existe.");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("No guarda una configuración inválida")
    void rejectsInvalidAvailability() {
        when(professionalCatalog.findById(PROFESSIONAL_ID)).thenReturn(Optional.of(PROFESSIONAL));

        assertThatThrownBy(() -> service.save(command(5))).isInstanceOf(BusinessRuleViolationException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Consulta la disponibilidad guardada de un profesional")
    void findsAvailability() {
        when(repository.findByProfessionalId(PROFESSIONAL_ID)).thenReturn(Optional.empty());

        assertThat(service.findByProfessional(PROFESSIONAL_ID)).isEmpty();
    }

    private static SaveAvailabilityCommand command(int slotMinutes) {
        return new SaveAvailabilityCommand(PROFESSIONAL_ID, Set.of(MONDAY, TUESDAY), LocalTime.of(8, 0),
                LocalTime.of(12, 0), slotMinutes);
    }
}
