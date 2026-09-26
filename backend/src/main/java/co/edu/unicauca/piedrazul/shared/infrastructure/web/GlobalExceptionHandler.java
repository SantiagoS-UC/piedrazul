package co.edu.unicauca.piedrazul.shared.infrastructure.web;

import co.edu.unicauca.piedrazul.shared.domain.AuthenticationFailedException;
import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import co.edu.unicauca.piedrazul.shared.domain.DuplicateResourceException;
import co.edu.unicauca.piedrazul.shared.domain.ResourceNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Traduce las excepciones de todos los módulos a respuestas con mensajes comprensibles. Nunca se
 * envían al usuario trazas ni mensajes técnicos.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DuplicateResourceException.class)
    ResponseEntity<ApiError> handleDuplicate(DuplicateResourceException ex) {
        return withOptionalField(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    ResponseEntity<ApiError> handleBusinessRule(BusinessRuleViolationException ex) {
        return withOptionalField(HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.of(ex.getMessage()));
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    ResponseEntity<ApiError> handleAuthenticationFailed(AuthenticationFailedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiError.of(ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiError.of("No tienes permiso para realizar esta acción."));
    }

    // Dos peticiones simultáneas pueden pasar las validaciones previas y chocar en una restricción
    // única de la base de datos.
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Conflicto de integridad de datos", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of("Otra operación acaba de modificar esta información. Intenta de nuevo."));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        log.error("Error no controlado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of("Ocurrió un error inesperado. Intenta de nuevo en unos minutos."));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(new ApiError("Revisa los campos marcados.", fieldErrors));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return ResponseEntity.badRequest()
                .body(ApiError.of("Algunos datos enviados no tienen el formato esperado."));
    }

    private static ResponseEntity<ApiError> withOptionalField(HttpStatus status,
            BusinessRuleViolationException ex) {
        ApiError body = ex.field()
                .map(field -> ApiError.of(field, ex.getMessage()))
                .orElseGet(() -> ApiError.of(ex.getMessage()));
        return ResponseEntity.status(status).body(body);
    }
}
