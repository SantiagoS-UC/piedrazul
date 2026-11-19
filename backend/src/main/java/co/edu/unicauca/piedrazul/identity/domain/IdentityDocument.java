package co.edu.unicauca.piedrazul.identity.domain;

import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import java.util.Locale;
import java.util.regex.Pattern;

public record IdentityDocument(DocumentType type, String number) {

    private static final Pattern NUMERIC = Pattern.compile("^\\d{5,15}$");
    // El pasaporte es el único documento que puede incluir letras.
    private static final Pattern PASSPORT = Pattern.compile("^[A-Z0-9]{5,20}$");

    public IdentityDocument {
        if (type == null) {
            throw new BusinessRuleViolationException("documentType", "Selecciona el tipo de documento.");
        }
        if (number == null || number.isBlank()) {
            throw new BusinessRuleViolationException("documentNumber", "El número de documento es obligatorio.");
        }
        number = number.trim().toUpperCase(Locale.ROOT);
        if (type == DocumentType.PA && !PASSPORT.matcher(number).matches()) {
            throw new BusinessRuleViolationException("documentNumber",
                    "El pasaporte debe tener entre 5 y 20 letras o números, sin espacios.");
        }
        if (type != DocumentType.PA && !NUMERIC.matcher(number).matches()) {
            throw new BusinessRuleViolationException("documentNumber",
                    "El número de documento debe tener solo números, entre 5 y 15 dígitos.");
        }
    }
}
