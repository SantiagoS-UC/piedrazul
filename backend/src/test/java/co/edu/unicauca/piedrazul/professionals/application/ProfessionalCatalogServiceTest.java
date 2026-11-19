package co.edu.unicauca.piedrazul.professionals.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import co.edu.unicauca.piedrazul.professionals.ProfessionalSummary;
import co.edu.unicauca.piedrazul.professionals.domain.Professional;
import co.edu.unicauca.piedrazul.professionals.domain.ProfessionalRepository;
import co.edu.unicauca.piedrazul.professionals.domain.Specialty;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProfessionalCatalogServiceTest {

    @Mock
    private ProfessionalRepository repository;

    private ProfessionalCatalogService service;

    @BeforeEach
    void setUp() {
        service = new ProfessionalCatalogService(repository);
    }

    @Test
    @DisplayName("Lista los profesionales en orden alfabético por apellido")
    void listsAlphabetically() {
        when(repository.findAll()).thenReturn(List.of(
                new Professional(UUID.randomUUID(), "Paula", "Zambrano", Specialty.PHYSIOTHERAPY, true),
                new Professional(UUID.randomUUID(), "Camilo", "Benavides", Specialty.CHIROPRACTIC, true),
                new Professional(UUID.randomUUID(), "Laura", "Martínez", Specialty.GENERAL_MEDICINE, true)));

        List<String> lastNames = service.findAll().stream().map(ProfessionalSummary::lastName).toList();

        assertThat(lastNames).containsExactly("Benavides", "Martínez", "Zambrano");
    }

    @Test
    @DisplayName("Entrega el código y el nombre de la especialidad")
    void mapsSpecialty() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id))
                .thenReturn(Optional.of(new Professional(id, "Diana", "Rojas", Specialty.NEURAL_THERAPY, true)));

        ProfessionalSummary summary = service.findById(id).orElseThrow();

        assertThat(summary.specialty()).isEqualTo("NEURAL_THERAPY");
        assertThat(summary.specialtyName()).isEqualTo("Terapia neural");
        assertThat(summary.fullName()).isEqualTo("Diana Rojas");
        assertThat(summary.active()).isTrue();
    }

    @Test
    @DisplayName("Responde vacío si el profesional no existe")
    void unknownProfessional() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThat(service.findById(id)).isEmpty();
    }
}
