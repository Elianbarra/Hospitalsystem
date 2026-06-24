package cl.rednorte.bff.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * Campos opcionales para reasignar o actualizar una cita.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateAppointmentRequestDTO(
        LocalDateTime scheduledAt,
        String status,
        String notes
) {}
