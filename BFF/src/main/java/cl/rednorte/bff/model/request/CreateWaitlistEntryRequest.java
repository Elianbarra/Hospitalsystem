package cl.rednorte.bff.model.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Inscripción de un paciente en la lista de espera para una especialidad.
 * La prioridad es asignada por el ms-waitlist según criterios clínicos.
 */
public record CreateWaitlistEntryRequest(
        @NotBlank String patientId,
        @NotBlank String specialty,
        String reason,
        String priority   // HIGH | MEDIUM | LOW — si es null, el ms-waitlist asigna automáticamente
) {}
