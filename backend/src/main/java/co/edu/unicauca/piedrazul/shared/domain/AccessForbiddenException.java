package co.edu.unicauca.piedrazul.shared.domain;

/**
 * Acceso negado por una regla de negocio aunque el rol sea correcto, por ejemplo un profesional
 * que pide la agenda de otro.
 */
public class AccessForbiddenException extends RuntimeException {

    public AccessForbiddenException(String message) {
        super(message);
    }
}
