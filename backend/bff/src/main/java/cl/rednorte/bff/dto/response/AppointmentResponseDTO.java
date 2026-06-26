package cl.rednorte.bff.dto.response;

import java.time.LocalDateTime;

public record AppointmentResponseDTO(
        String id,
        String patientId,
        String doctorId,
        String specialty,
        String appointmentType,
        LocalDateTime scheduledAt,
        String status,
        String cancelledBy,
        String notes,
        String createdAt
) {}
