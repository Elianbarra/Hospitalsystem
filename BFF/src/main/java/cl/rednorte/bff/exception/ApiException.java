package cl.rednorte.bff.exception;

/**
 * Excepción tipada que encapsula errores HTTP provenientes de los microservicios.
 * Se lanza en la capa Service y es capturada por {@link GlobalExceptionHandler}.
 */
public class ApiException extends RuntimeException {

    private final int status;
    private final Object details;

    public ApiException(int status, String message, Object details) {
        super(message);
        this.status = status;
        this.details = details;
    }

    public ApiException(int status, String message) {
        this(status, message, null);
    }

    public int getStatus() {
        return status;
    }

    public Object getDetails() {
        return details;
    }
}
