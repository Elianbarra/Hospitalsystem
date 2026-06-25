package cl.rednorte.bff.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Solicitud de creación de cita médica.
 * El ms-appointment validará disponibilidad del doctor en el horario indicado.
 */
public record CreateAppointmentRequestDTO(
        @NotBlank String patientId,
        @NotBlank String doctorId,
        @NotBlank String specialty,
        /** CONSULTA (defecto) o CIRUGIA */
        String appointmentType,
        @NotNull @FutureOrPresent LocalDateTime scheduledAt,
        String notes
) {}
