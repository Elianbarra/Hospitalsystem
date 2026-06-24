package cl.rednorte.bff.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Inscripción de un paciente en la lista de espera para una especialidad.
 */
public record CreateWaitlistEntryRequestDTO(
        @NotBlank String patientId,
        @NotBlank String specialty,
        /** CONSULTA (defecto) o CIRUGIA */
        String appointmentType,
        /** true si el paciente tiene riesgo vital severo */
        Boolean vitalRisk,
        String notes
) {}
