package cl.rednorte.bff.dto.response;

public record WaitlistEntryResponseDTO(
        String id,
        String patientId,
        String specialty,
        String appointmentType,
        String priority,
        Boolean vitalRisk,
        String status,
        String notes,
        String createdAt,
        String requeuedAt,
        String updatedAt
) {}
