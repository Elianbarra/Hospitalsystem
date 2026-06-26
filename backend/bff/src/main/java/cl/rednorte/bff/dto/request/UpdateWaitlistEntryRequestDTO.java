package cl.rednorte.bff.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Campos opcionales para actualizar una entrada en lista de espera.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateWaitlistEntryRequestDTO(
        String status,
        String priority,
        String notes
) {}
