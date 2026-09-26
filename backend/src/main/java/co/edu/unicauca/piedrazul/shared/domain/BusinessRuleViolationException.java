package co.edu.unicauca.piedrazul.shared.domain;

import java.util.Optional;

/**
 * Una regla de negocio no se cumple. El mensaje se muestra tal cual al usuario, así que debe estar
 * redactado en español sencillo y decir cómo corregir el problema.
 */
public class BusinessRuleViolationException extends RuntimeException {

    private final String field;

    public BusinessRuleViolationException(String message) {
        this(null, message);
    }

    /**
     * @param field nombre del campo de la petición afectado, para que el frontend muestre el mensaje
     *              junto a él
     */
    public BusinessRuleViolationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public Optional<String> field() {
        return Optional.ofNullable(field);
    }
}
