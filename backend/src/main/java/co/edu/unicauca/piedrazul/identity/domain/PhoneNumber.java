package co.edu.unicauca.piedrazul.identity.domain;

import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import java.util.regex.Pattern;

/**
 * Número celular colombiano de 10 dígitos. Se aceptan espacios o guiones al escribirlo.
 */
public record PhoneNumber(String value) {

    private static final Pattern TEN_DIGITS = Pattern.compile("^\\d{10}$");

    public PhoneNumber {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleViolationException("phone", "El teléfono es obligatorio.");
        }
        value = value.replaceAll("[\\s-]", "");
        if (!TEN_DIGITS.matcher(value).matches()) {
            throw new BusinessRuleViolationException("phone",
                    "El teléfono debe tener 10 dígitos, por ejemplo: 3001234567.");
        }
    }
}
