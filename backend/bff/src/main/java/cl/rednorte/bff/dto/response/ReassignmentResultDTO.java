package cl.rednorte.bff.dto.response;

/**
 * Resultado de la orquestación de reasignación de citas.
 * outcome: REASIGNADO | HORA_LIBERADA_SIN_REASIGNAR | PACIENTE_REUBICADO_AL_FINAL
 */
public record ReassignmentResultDTO(
        AppointmentResponseDTO cancelledAppointment,
        AppointmentResponseDTO newAppointment,
        WaitlistEntryResponseDTO waitlistEntry,
        String outcome
) {}
