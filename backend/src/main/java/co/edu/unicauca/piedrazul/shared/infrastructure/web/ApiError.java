package co.edu.unicauca.piedrazul.shared.infrastructure.web;

import java.util.Map;

/**
 * Cuerpo de todas las respuestas de error de la API.
 *
 * @param message     mensaje general para el usuario
 * @param fieldErrors mensaje por campo del formulario, vacío si el error no es de un campo
 */
public record ApiError(String message, Map<String, String> fieldErrors) {

    public static ApiError of(String message) {
        return new ApiError(message, Map.of());
    }

    public static ApiError of(String field, String message) {
        return new ApiError(message, Map.of(field, message));
    }
}
