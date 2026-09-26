package co.edu.unicauca.piedrazul.identity.domain;

import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Correo normalizado en minúsculas, para que "Ana@Gmail.com" y "ana@gmail.com" sean la misma cuenta.
 */
public record Email(String value) {

    private static final Pattern FORMAT = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int MAX_LENGTH = 254;

    public Email {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleViolationException("email", "El correo electrónico es obligatorio.");
        }
        value = value.trim().toLowerCase(Locale.ROOT);
        if (value.length() > MAX_LENGTH || !FORMAT.matcher(value).matches()) {
            throw new BusinessRuleViolationException("email",
                    "Escribe un correo válido, por ejemplo: nombre@gmail.com.");
        }
    }
}
