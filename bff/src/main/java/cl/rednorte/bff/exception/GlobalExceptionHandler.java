package cl.rednorte.bff.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Manejo centralizado de excepciones para todas las rutas del BFF.
 * Traduce {@link ApiException} y errores de validación a respuestas HTTP estructuradas.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Errores provenientes de los microservicios (4xx / 5xx reenviados). */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException ex) {
        log.warn("ApiException [{}]: {}", ex.getStatus(), ex.getMessage());
        return ResponseEntity
                .status(ex.getStatus())
                .body(Map.of(
                        "message", ex.getMessage(),
                        "details", ex.getDetails() != null ? ex.getDetails() : Map.of()
                ));
    }

    /** Errores de validación de @Valid en los request bodies. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        return ResponseEntity
                .badRequest()
                .body(Map.of("message", "Validation failed", "details", errors));
    }

    /** Fallback para excepciones no contempladas. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        log.error("Unexpected BFF error", ex);
        return ResponseEntity
                .internalServerError()
                .body(Map.of("message", "Internal server error"));
    }
}
