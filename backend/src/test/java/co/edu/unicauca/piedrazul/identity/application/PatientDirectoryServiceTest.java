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
class PatientDirectoryServiceTest {

    @Mock
    private PatientRepository patientRepository;

    private PatientDirectoryService service;

    @BeforeEach
    void setUp() {
        service = new PatientDirectoryService(patientRepository);
    }

    @Test
    @DisplayName("Entrega los datos de contacto de los pacientes solicitados")
    void returnsSummaries() {
        UUID id = UUID.randomUUID();
        Patient patient = Patient.restore(id, new PersonName("Ana", "María", "Gómez", null),
                new IdentityDocument(DocumentType.CC, "1061000001"), LocalDate.of(1958, 3, 14), Gender.FEMALE,
                new PhoneNumber("3001234567"));
        when(patientRepository.findAllById(List.of(id))).thenReturn(List.of(patient));

        List<PatientSummary> summaries = service.findByIds(List.of(id));

        assertThat(summaries).singleElement().satisfies(summary -> {
            assertThat(summary.id()).isEqualTo(id);
            assertThat(summary.givenNames()).isEqualTo("Ana María");
            assertThat(summary.lastNames()).isEqualTo("Gómez");
            assertThat(summary.documentType()).isEqualTo("CC");
            assertThat(summary.documentNumber()).isEqualTo("1061000001");
            assertThat(summary.phone()).isEqualTo("3001234567");
        });
    }

    @Test
    @DisplayName("Con una lista vacía no consulta la base de datos")
    void emptyIdsSkipQuery() {
        assertThat(service.findByIds(List.of())).isEmpty();
        verifyNoInteractions(patientRepository);
    }
}
