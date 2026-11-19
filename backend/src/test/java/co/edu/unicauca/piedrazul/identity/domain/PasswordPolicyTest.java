package co.edu.unicauca.piedrazul.identity.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PasswordPolicyTest {

    private final PasswordPolicy policy = new PasswordPolicy();

    @Test
    @DisplayName("Acepta una contraseña de 8 caracteres con letras y números")
    void acceptsValidPassword() {
        assertThatCode(() -> policy.validate("admin-1234")).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Rechaza una contraseña vacía")
    void rejectsBlank(String password) {
        assertThatThrownBy(() -> policy.validate(password))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("La contraseña es obligatoria.");
    }

    @Test
    @DisplayName("Rechaza una contraseña de menos de 8 caracteres")
    void rejectsShortPassword() {
        assertThatThrownBy(() -> policy.validate("abc123"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("al menos 8 caracteres");
    }

    @ParameterizedTest
    @ValueSource(strings = {"solamenteletras", "1234567890"})
    @DisplayName("Rechaza una contraseña que no combina letras y números")
    void rejectsWithoutLettersAndDigits(String password) {
        assertThatThrownBy(() -> policy.validate(password))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("La contraseña debe combinar letras y números.");
    }
}
