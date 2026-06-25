package cl.rednorte.bff.controller;

import cl.rednorte.bff.dto.request.CreateWaitlistEntryRequestDTO;
import cl.rednorte.bff.dto.request.UpdateWaitlistEntryRequestDTO;
import cl.rednorte.bff.dto.response.WaitlistEntryResponseDTO;
import cl.rednorte.bff.service.WaitlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller MVC — capa Waitlist (Lista de espera).
 *
 * Rutas expuestas por el BFF:
 *   GET    /api/waitlist                      → ms-waitlist GET    /api/waitlist
 *   POST   /api/waitlist                      → ms-waitlist POST   /api/waitlist
 *   GET    /api/waitlist/{id}                 → ms-waitlist GET    /api/waitlist/{id}
 *   PUT    /api/waitlist/{id}                 → ms-waitlist PUT    /api/waitlist/{id}
 *   PUT    /api/waitlist/{id}/cancel          → ms-waitlist PUT    /api/waitlist/{id}/cancel
 *   DELETE /api/waitlist/{id}                 → ms-waitlist DELETE /api/waitlist/{id}
 *   GET    /api/waitlist/patient/{patientId}           → ms-waitlist GET    /api/waitlist/patient/{patientId}
 *   GET    /api/waitlist/specialty/{specialty}/next    → ms-waitlist GET    /api/waitlist/specialty/{specialty}/next
 *   PUT    /api/waitlist/{id}/requeue                  → ms-waitlist PUT    /api/waitlist/{id}/requeue
 */
@RestController
@RequestMapping("/api/waitlist")
@Tag(name = "Lista de espera", description = "Gestión de pacientes en espera de atención médica")
public class WaitlistController {

    private final WaitlistService waitlistService;

    public WaitlistController(WaitlistService waitlistService) {
        this.waitlistService = waitlistService;
    }

    @Operation(summary = "Listar lista de espera", description = "Devuelve todos los pacientes en espera")
    @GetMapping
    public ResponseEntity<List<WaitlistEntryResponseDTO>> getAll() {
        return ResponseEntity.ok(waitlistService.getAll());
    }

    @Operation(summary = "Agregar a lista de espera", description = "Ingresa un paciente a la lista de espera. Prioridad inicial: NORMAL")
    @PostMapping
    public ResponseEntity<WaitlistEntryResponseDTO> enqueue(@Valid @RequestBody CreateWaitlistEntryRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(waitlistService.enqueue(request));
    }

    @Operation(summary = "Obtener entrada", description = "Devuelve una entrada de la lista de espera por su ID")
    @GetMapping("/{id}")
    public ResponseEntity<WaitlistEntryResponseDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(waitlistService.getById(id));
    }

    @Operation(summary = "Actualizar entrada", description = "Médico/Admin actualiza prioridad (NORMAL|URGENTE|CRITICO) o estado")
    @PutMapping("/{id}")
    public ResponseEntity<WaitlistEntryResponseDTO> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateWaitlistEntryRequestDTO request) {
        return ResponseEntity.ok(waitlistService.update(id, request));
    }

    @Operation(summary = "Cancelar entrada", description = "Marca como CANCELLED la entrada de un paciente en lista de espera")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<WaitlistEntryResponseDTO> cancel(@PathVariable String id) {
        return ResponseEntity.ok(waitlistService.cancel(id));
    }

    @Operation(summary = "Eliminar de lista de espera", description = "Elimina permanentemente una entrada de la lista de espera")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable String id) {
        waitlistService.remove(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Lista de espera por paciente", description = "Devuelve las entradas de lista de espera de un paciente específico")
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<WaitlistEntryResponseDTO>> getByPatient(@PathVariable String patientId) {
        return ResponseEntity.ok(waitlistService.getByPatient(patientId));
    }

    @Operation(summary = "Lista de espera por especialidad", description = "Devuelve todas las entradas en espera de una especialidad — útil para calcular posición")
    @GetMapping("/specialty/{specialty}")
    public ResponseEntity<List<WaitlistEntryResponseDTO>> getBySpecialty(@PathVariable String specialty) {
        return ResponseEntity.ok(waitlistService.getBySpecialty(specialty));
    }

    @Operation(summary = "Siguiente en cola", description = "Devuelve el siguiente paciente en cola (WAITING) para una especialidad. 204 si la cola está vacía.")
    @GetMapping("/specialty/{specialty}/next")
    public ResponseEntity<WaitlistEntryResponseDTO> getNextForSpecialty(@PathVariable String specialty) {
        WaitlistEntryResponseDTO next = waitlistService.getNextForSpecialty(specialty);
        return next != null ? ResponseEntity.ok(next) : ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reencolar al final", description = "Mueve al paciente al final de la cola cuando cancela su cita (resetea requeuedAt)")
    @PutMapping("/{id}/requeue")
    public ResponseEntity<WaitlistEntryResponseDTO> requeueToEnd(@PathVariable String id) {
        return ResponseEntity.ok(waitlistService.requeueToEnd(id));
    }
}
