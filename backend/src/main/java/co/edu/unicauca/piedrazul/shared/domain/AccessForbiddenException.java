package co.edu.unicauca.piedrazul.shared.domain;

/**
 * El usuario tiene el rol correcto, pero la regla de negocio no le permite ver o modificar este
 * recurso en particular; por ejemplo, un profesional que pide la agenda de otro profesional.
 */
public class AccessForbiddenException extends RuntimeException {

    public AccessForbiddenException(String message) {
        super(message);
    }
}
