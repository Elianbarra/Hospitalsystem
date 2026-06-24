package cl.rednorte.bff.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Campos opcionales para actualizar una entrada en lista de espera.
 * Usado por médicos/admin para asignar o cambiar prioridad.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateWaitlistEntryRequest(
        String status,    // WAITING | NOTIFIED | ASSIGNED | CANCELLED
        String priority,  // NORMAL | URGENTE | CRITICO
        String notes
) {}
