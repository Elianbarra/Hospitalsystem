package cl.rednorte.bff.model.response;

import java.time.LocalDateTime;

public record AppointmentResponse(
        String id,
        String patientId,
        String doctorId,
        String specialty,
        String appointmentType,  // CONSULTA | CIRUGIA
        LocalDateTime scheduledAt,
        String status,           // PENDING | CONFIRMED | CANCELLED | COMPLETED
        String cancelledBy,      // DOCTOR | PATIENT | null
        String notes,
        String createdAt
) {}
