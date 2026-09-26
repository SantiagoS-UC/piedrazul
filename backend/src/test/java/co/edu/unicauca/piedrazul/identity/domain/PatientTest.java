package co.edu.unicauca.piedrazul.identity.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PatientTest {

    private static final UUID ID = UUID.randomUUID();
    private static final PersonName NAME = new PersonName("Ana", null, "Gómez", null);
    private static final IdentityDocument DOCUMENT = new IdentityDocument(DocumentType.CC, "1061000001");
    private static final PhoneNumber PHONE = new PhoneNumber("3001234567");
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 25);

    @Test
    @DisplayName("Registra un paciente con datos válidos")
    void registersPatient() {
        LocalDate birthDate = LocalDate.of(1958, 3, 14);

        Patient patient = Patient.register(ID, NAME, DOCUMENT, birthDate, Gender.FEMALE, PHONE, TODAY);

        assertThat(patient.id()).isEqualTo(ID);
        assertThat(patient.birthDate()).isEqualTo(birthDate);
        assertThat(patient.gender()).isEqualTo(Gender.FEMALE);
    }

    @Test
    @DisplayName("Acepta un paciente nacido hoy")
    void acceptsBirthDateToday() {
        Patient patient = Patient.register(ID, NAME, DOCUMENT, TODAY, Gender.MALE, PHONE, TODAY);

        assertThat(patient.birthDate()).isEqualTo(TODAY);
    }

    @Test
    @DisplayName("Exige la fecha de nacimiento")
    void requiresBirthDate() {
        assertThatThrownBy(() -> Patient.register(ID, NAME, DOCUMENT, null, Gender.FEMALE, PHONE, TODAY))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("La fecha de nacimiento es obligatoria.");
    }

    @Test
    @DisplayName("Rechaza una fecha de nacimiento futura")
    void rejectsFutureBirthDate() {
        LocalDate tomorrow = TODAY.plusDays(1);

        assertThatThrownBy(() -> Patient.register(ID, NAME, DOCUMENT, tomorrow, Gender.FEMALE, PHONE, TODAY))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("La fecha de nacimiento no puede ser futura.");
    }

    @Test
    @DisplayName("Exige el género")
    void requiresGender() {
        assertThatThrownBy(() -> Patient.register(ID, NAME, DOCUMENT, TODAY, null, PHONE, TODAY))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Selecciona el género.");
    }
}
