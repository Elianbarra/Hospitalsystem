package cl.rednorte.bff.model.response;

public record WaitlistEntryResponse(
        String id,
        String patientId,
        String specialty,
        String priority,   // NORMAL | URGENTE | CRITICO
        String status,     // WAITING | NOTIFIED | ASSIGNED | CANCELLED
        String notes,
        String createdAt,
        String updatedAt
) {}
