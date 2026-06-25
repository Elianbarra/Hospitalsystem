package cl.rednorte.bff.controller;

import cl.rednorte.bff.dto.response.ReassignmentResultDTO;
import cl.rednorte.bff.service.ReassignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * BFF orchestration endpoints for appointment cancellation and reassignment.
 *
 * Doctor cancels → automatic reassignment to next patient in waitlist.
 * Patient cancels → patient is requeued to the end of the waitlist.
 *
 * The frontend always calls THESE endpoints — never ms-appointment directly.
 */
@RestController
@RequestMapping("/api/reassignment")
@Tag(name = "Reassignment", description = "BFF orchestration for appointment cancellation and reassignment")
public class ReassignmentController {

    private final ReassignmentService reassignmentService;

    public ReassignmentController(ReassignmentService reassignmentService) {
        this.reassignmentService = reassignmentService;
    }

    @Operation(
        summary = "Doctor cancels appointment",
        description = """
            Orchestrates doctor cancellation:
            1. Cancels the appointment in ms-appointment (cancelledBy=DOCTOR).
            2. Finds the next patient in the waitlist for that specialty.
            3. Creates a new appointment for that patient.
            4. Marks the waitlist entry as OFFERED.
            """
    )
    @PutMapping("/cancel-doctor/{appointmentId}")
    public ResponseEntity<ReassignmentResultDTO> cancelByDoctor(
            @PathVariable String appointmentId,
            @RequestParam(required = false) String waitlistEntryId) {
        return ResponseEntity.ok(reassignmentService.cancelByDoctor(appointmentId, waitlistEntryId));
    }

    @Operation(
        summary = "Patient cancels appointment",
        description = """
            Orchestrates patient cancellation:
            1. Cancels the appointment in ms-appointment (cancelledBy=PATIENT).
            2. Requeues the patient to the END of the waitlist (requeuedAt=now).
            """
    )
    @PutMapping("/cancel-patient/{appointmentId}")
    public ResponseEntity<ReassignmentResultDTO> cancelByPatient(
            @PathVariable String appointmentId,
            @RequestParam(required = false) String waitlistEntryId) {
        return ResponseEntity.ok(reassignmentService.cancelByPatient(appointmentId, waitlistEntryId));
    }
}
