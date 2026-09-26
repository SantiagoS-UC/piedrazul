package co.edu.unicauca.piedrazul.identity.domain;

import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;

/**
 * Requisitos mínimos de una contraseña: longitud y combinación de letras y números. Se valida
 * antes de cifrarla, porque después ya no es posible.
 */
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;

    public void validate(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new BusinessRuleViolationException("password", "La contraseña es obligatoria.");
        }
        if (rawPassword.length() < MIN_LENGTH) {
            throw new BusinessRuleViolationException("password",
                    "La contraseña debe tener al menos " + MIN_LENGTH + " caracteres.");
        }
        boolean hasLetter = rawPassword.chars().anyMatch(Character::isLetter);
        boolean hasDigit = rawPassword.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            throw new BusinessRuleViolationException("password", "La contraseña debe combinar letras y números.");
        }
    }
}
