package co.edu.unicauca.piedrazul.identity.domain;

import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Nombre completo al estilo colombiano: dos nombres y dos apellidos, donde el segundo de cada uno
 * es opcional.
 */
public record PersonName(String firstName, String middleName, String firstLastName, String secondLastName) {

    private static final Pattern LETTERS = Pattern.compile("^[\\p{L}' -]+$");
    private static final int MAX_LENGTH = 60;

    public PersonName {
        firstName = required("firstName", firstName, "El primer nombre es obligatorio.");
        middleName = optional("middleName", middleName);
        firstLastName = required("firstLastName", firstLastName, "El primer apellido es obligatorio.");
        secondLastName = optional("secondLastName", secondLastName);
    }

    public String fullName() {
        return Stream.of(firstName, middleName, firstLastName, secondLastName)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
    }

    /**
     * Primer nombre y primer apellido, como se muestra en el menú de la aplicación.
     */
    public String shortName() {
        return firstName + " " + firstLastName;
    }

    private static String required(String field, String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleViolationException(field, message);
        }
        return validated(field, value);
    }

    private static String optional(String field, String value) {
        return value == null || value.isBlank() ? null : validated(field, value);
    }

    private static String validated(String field, String value) {
        String trimmed = value.trim().replaceAll("\\s+", " ");
        if (trimmed.length() > MAX_LENGTH || !LETTERS.matcher(trimmed).matches()) {
            throw new BusinessRuleViolationException(field,
                    "Solo puede contener letras y máximo " + MAX_LENGTH + " caracteres.");
        }
        return trimmed;
    }
}
