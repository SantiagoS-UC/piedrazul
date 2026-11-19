package co.edu.unicauca.piedrazul.identity.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTest {

    @Test
    @DisplayName("Normaliza el correo a minúsculas y sin espacios")
    void normalizesValue() {
        assertThat(new Email("  Ana.Gomez@Gmail.COM ").value()).isEqualTo("ana.gomez@gmail.com");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("Rechaza un correo vacío")
    void rejectsBlank(String value) {
        assertThatThrownBy(() -> new Email(value))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("El correo electrónico es obligatorio.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana", "ana@", "ana@gmail", "ana gomez@gmail.com", "@gmail.com"})
    @DisplayName("Rechaza un correo sin formato válido")
    void rejectsInvalidFormat(String value) {
        assertThatThrownBy(() -> new Email(value))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Escribe un correo válido");
    }

    @Test
    @DisplayName("Dos correos que solo difieren en mayúsculas son iguales")
    void equalityIgnoresCase() {
        assertThat(new Email("ANA@gmail.com")).isEqualTo(new Email("ana@gmail.com"));
    }
}
