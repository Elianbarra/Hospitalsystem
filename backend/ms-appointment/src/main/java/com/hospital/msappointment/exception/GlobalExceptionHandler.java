package com.hospital.msappointment.exception;

import io.sentry.Sentry;
import io.sentry.SentryLevel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 esperado — no se reporta (ruido innecesario en GlitchTip)
    @ExceptionHandler(AppointmentNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(AppointmentNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // Conflicto de horario — WARNING: puede indicar race condition o bug en el frontend
    @ExceptionHandler(AppointmentConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(AppointmentConflictException ex) {
        Sentry.withScope(scope -> {
            scope.setLevel(SentryLevel.WARNING);
            scope.setTag("exception.type", "scheduling_conflict");
            Sentry.captureException(ex);
        });
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // Usuario no encontrado — ERROR: indica inconsistencia entre ms-appointment y ms-user
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        Sentry.withScope(scope -> {
            scope.setTag("exception.type", "user_not_found");
            scope.setTag("microservice.origin", "ms-user");
            Sentry.captureException(ex);
        });
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // Acceso denegado — WARNING: puede indicar intento de acceso no autorizado
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        Sentry.withScope(scope -> {
            scope.setLevel(SentryLevel.WARNING);
            scope.setTag("exception.type", "access_denied");
            Sentry.captureException(ex);
        });
        return buildResponse(HttpStatus.FORBIDDEN, "Acceso denegado");
    }

    // Validación — no se reporta (input inválido del cliente, no es un bug del sistema)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage()));
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("errors", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    // Cualquier excepción no prevista — ERROR: siempre se reporta, son bugs reales
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        Sentry.withScope(scope -> {
            scope.setTag("exception.type", "unhandled");
            String podName = System.getenv("POD_NAME");
            String nodeName = System.getenv("NODE_NAME");
            if (podName  != null) scope.setTag("pod",  podName);
            if (nodeName != null) scope.setTag("node", nodeName);
            Sentry.captureException(ex);
        });
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
