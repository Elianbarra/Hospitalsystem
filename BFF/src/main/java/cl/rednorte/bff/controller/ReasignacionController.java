package cl.rednorte.bff.controller;

import cl.rednorte.bff.service.ReasignacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints de reasignación orquestados por el BFF.
 *
 * Cuando el médico cancela → reasignación automática al siguiente en cola.
 * Cuando el paciente cancela → el paciente queda al final de la lista de espera.
 *
 * El frontend siempre llama a ESTOS endpoints — nunca a ms-appointment directamente.
 */
@RestController
@RequestMapping("/api/reasignacion")
@Tag(name = "Reasignación", description = "Orquestación BFF para cancelación y reasignación de citas")
public class ReasignacionController {

    private final ReasignacionService reasignacionService;

    public ReasignacionController(ReasignacionService reasignacionService) {
        this.reasignacionService = reasignacionService;
    }

    @Operation(
        summary = "Médico cancela cita",
        description = """
            Orquesta la cancelación por médico:
            1. Cancela la cita en ms-appointment (cancelledBy=DOCTOR).
            2. Busca el siguiente en lista de espera de esa especialidad.
            3. Crea nueva cita para ese paciente.
            4. Marca la entrada de waitlist como OFFERED.

            Parámetro opcional waitlistEntryId: si se conoce la entrada de waitlist del
            siguiente paciente, se pasa directamente; si no, el BFF la resuelve automáticamente.
            """
    )
    @PutMapping("/cancel-doctor/{appointmentId}")
    public ResponseEntity<ReasignacionService.ReasignacionResult> cancelByDoctor(
            @PathVariable String appointmentId,
            @RequestParam(required = false) String waitlistEntryId) {
        return ResponseEntity.ok(reasignacionService.cancelByDoctor(appointmentId, waitlistEntryId));
    }

    @Operation(
        summary = "Paciente cancela cita",
        description = """
            Orquesta la cancelación por paciente:
            1. Cancela la cita en ms-appointment (cancelledBy=PATIENT).
            2. Reencola al paciente al FINAL de la lista de espera (requeuedAt=now).
               El paciente pierde la cita pero conserva su lugar en la lista.

            Parámetro opcional waitlistEntryId: ID de la entrada de waitlist del paciente
            para poder reubicarlo en la cola.
            """
    )
    @PutMapping("/cancel-patient/{appointmentId}")
    public ResponseEntity<ReasignacionService.ReasignacionResult> cancelByPatient(
            @PathVariable String appointmentId,
            @RequestParam(required = false) String waitlistEntryId) {
        return ResponseEntity.ok(reasignacionService.cancelByPatient(appointmentId, waitlistEntryId));
    }
}
