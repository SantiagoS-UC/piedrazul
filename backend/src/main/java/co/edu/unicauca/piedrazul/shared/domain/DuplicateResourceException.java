package co.edu.unicauca.piedrazul.shared.domain;

/**
 * El dato que se intenta registrar ya existe, por ejemplo un correo o un documento.
 */
public class DuplicateResourceException extends BusinessRuleViolationException {

    public DuplicateResourceException(String field, String message) {
        super(field, message);
    }
}
