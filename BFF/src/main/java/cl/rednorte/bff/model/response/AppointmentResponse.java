package cl.rednorte.bff.model.response;

import java.time.LocalDateTime;

public record AppointmentResponse(
        String id,
        String patientId,
        String doctorId,
        String specialty,
        LocalDateTime scheduledAt,
        String status,        // PENDING | CONFIRMED | CANCELLED | COMPLETED
        String notes,
        String createdAt
) {}
