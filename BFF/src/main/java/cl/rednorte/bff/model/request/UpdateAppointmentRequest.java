package cl.rednorte.bff.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * Campos opcionales para reasignar o actualizar una cita (ej. cancelar, reagendar).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateAppointmentRequest(
        LocalDateTime scheduledAt,
        String status,
        String notes
) {}
