package cl.rednorte.bff.model.response;

public record WaitlistEntryResponse(
        String id,
        String patientId,
        String specialty,
        String priority,      // HIGH | MEDIUM | LOW
        String status,        // WAITING | CALLED | ASSIGNED | CANCELLED
        String reason,
        String requestDate,
        String estimatedDate
) {}
