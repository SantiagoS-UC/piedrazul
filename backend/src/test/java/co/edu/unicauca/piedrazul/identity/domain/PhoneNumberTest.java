package co.edu.unicauca.piedrazul.identity.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PhoneNumberTest {

    @Test
    @DisplayName("Acepta un celular escrito con espacios o guiones")
    void acceptsSeparators() {
        assertThat(new PhoneNumber("300 123-4567").value()).isEqualTo("3001234567");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Rechaza un teléfono vacío")
    void rejectsBlank(String value) {
        assertThatThrownBy(() -> new PhoneNumber(value))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("El teléfono es obligatorio.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"300123456", "30012345678", "300123456a", "+573001234567"})
    @DisplayName("Rechaza un teléfono que no tiene exactamente 10 dígitos")
    void rejectsWrongLength(String value) {
        assertThatThrownBy(() -> new PhoneNumber(value))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("10 dígitos");
    }
}
