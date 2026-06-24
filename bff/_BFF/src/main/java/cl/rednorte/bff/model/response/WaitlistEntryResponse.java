package cl.rednorte.bff.model.response;

public record WaitlistEntryResponse(
        String id,
        String patientId,
        String specialty,
        String appointmentType,  // CONSULTA | CIRUGIA
        String priority,         // NORMAL | URGENTE | CRITICO
        Boolean vitalRisk,
        String status,           // WAITING | OFFERED | NOTIFIED | ASSIGNED | CANCELLED
        String notes,
        String createdAt,
        String requeuedAt,
        String updatedAt
) {}
