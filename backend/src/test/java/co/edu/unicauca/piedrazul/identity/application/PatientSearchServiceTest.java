package co.edu.unicauca.piedrazul.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.edu.unicauca.piedrazul.identity.PatientSummary;
import co.edu.unicauca.piedrazul.identity.domain.DocumentType;
import co.edu.unicauca.piedrazul.identity.domain.Gender;
import co.edu.unicauca.piedrazul.identity.domain.IdentityDocument;
import co.edu.unicauca.piedrazul.identity.domain.Patient;
import co.edu.unicauca.piedrazul.identity.domain.PatientRepository;
import co.edu.unicauca.piedrazul.identity.domain.PersonName;
import co.edu.unicauca.piedrazul.identity.domain.PhoneNumber;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PatientSearchServiceTest {

    private static final Patient ANA = Patient.restore(UUID.randomUUID(),
            new PersonName("Ana", "María", "Gómez", "Ruiz"), new IdentityDocument(DocumentType.CC, "1061000001"),
            LocalDate.of(1958, 3, 14), Gender.FEMALE, new PhoneNumber("3001234567"));

    @Mock
    private PatientRepository patientRepository;

    private PatientSearchService service;

    @BeforeEach
    void setUp() {
        service = new PatientSearchService(patientRepository);
    }

    @Test
    @DisplayName("Sugiere los pacientes cuyo documento empieza por lo escrito")
    void suggestsByDocumentPrefix() {
        when(patientRepository.findByDocumentNumberStartingWith("106100", PatientSearchService.MAX_RESULTS))
                .thenReturn(List.of(ANA));

        List<PatientSummary> results = service.searchByDocument("106100");

        assertThat(results).singleElement().satisfies(patient -> {
            assertThat(patient.id()).isEqualTo(ANA.id());
            assertThat(patient.lastNames()).isEqualTo("Gómez Ruiz");
            assertThat(patient.documentNumber()).isEqualTo("1061000001");
        });
    }

    @Test
    @DisplayName("Ignora espacios y busca pasaportes en mayúsculas")
    void normalizesPrefix() {
        when(patientRepository.findByDocumentNumberStartingWith("AB12", PatientSearchService.MAX_RESULTS))
                .thenReturn(List.of());

        assertThat(service.searchByDocument("  ab12 ")).isEmpty();
    }

    @Test
    @DisplayName("Con menos de tres caracteres no consulta la base de datos")
    void requiresMinimumPrefix() {
        assertThat(service.searchByDocument("10")).isEmpty();
        assertThat(service.searchByDocument(null)).isEmpty();
        verifyNoInteractions(patientRepository);
    }
}
