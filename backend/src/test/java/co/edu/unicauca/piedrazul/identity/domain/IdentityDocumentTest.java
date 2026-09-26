package co.edu.unicauca.piedrazul.identity.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class IdentityDocumentTest {

    @Test
    @DisplayName("Acepta una cédula numérica")
    void acceptsNumericCitizenId() {
        IdentityDocument document = new IdentityDocument(DocumentType.CC, " 1061000001 ");

        assertThat(document.number()).isEqualTo("1061000001");
    }

    @Test
    @DisplayName("Acepta un pasaporte con letras y lo guarda en mayúsculas")
    void acceptsAlphanumericPassport() {
        assertThat(new IdentityDocument(DocumentType.PA, "ab12345").number()).isEqualTo("AB12345");
    }

    @Test
    @DisplayName("Exige el tipo de documento")
    void requiresType() {
        assertThatThrownBy(() -> new IdentityDocument(null, "1061000001"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Selecciona el tipo de documento.");
    }

    @Test
    @DisplayName("Exige el número de documento")
    void requiresNumber() {
        assertThatThrownBy(() -> new IdentityDocument(DocumentType.CC, " "))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("El número de documento es obligatorio.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234", "1234567890123456", "10610A0001", "1061.000.001"})
    @DisplayName("Rechaza una cédula con letras, símbolos o longitud inválida")
    void rejectsInvalidCitizenId(String number) {
        assertThatThrownBy(() -> new IdentityDocument(DocumentType.CC, number))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("solo números");
    }

    @Test
    @DisplayName("Rechaza un pasaporte con espacios")
    void rejectsPassportWithSpaces() {
        assertThatThrownBy(() -> new IdentityDocument(DocumentType.PA, "AB 12345"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("pasaporte");
    }
}
