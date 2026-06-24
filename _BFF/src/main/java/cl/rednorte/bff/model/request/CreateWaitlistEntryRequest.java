package cl.rednorte.bff.model.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Inscripción de un paciente en la lista de espera para una especialidad.
 * La prioridad la asigna el médico/admin mediante PUT /{id}; en creación
 * ms-waitlist asigna NORMAL por defecto.
 */
public record CreateWaitlistEntryRequest(
        @NotBlank String patientId,
        @NotBlank String specialty,
        /** CONSULTA (defecto) o CIRUGIA */
        String appointmentType,
        /** true si el paciente tiene riesgo vital severo */
        Boolean vitalRisk,
        String notes
) {}
