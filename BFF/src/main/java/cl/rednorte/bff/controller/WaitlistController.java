package cl.rednorte.bff.controller;

import cl.rednorte.bff.model.request.CreateWaitlistEntryRequest;
import cl.rednorte.bff.model.response.WaitlistEntryResponse;
import cl.rednorte.bff.service.WaitlistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller MVC — capa Waitlist (Lista de espera).
 *
 * Rutas expuestas por el BFF:
 *   GET    /api/waitlist                      → ms-waitlist GET  /api/waitlist
 *   POST   /api/waitlist                      → ms-waitlist POST /api/waitlist
 *   GET    /api/waitlist/{id}                 → ms-waitlist GET  /api/waitlist/{id}
 *   DELETE /api/waitlist/{id}                 → ms-waitlist DELETE /api/waitlist/{id}
 *   GET    /api/waitlist/patient/{patientId}  → ms-waitlist GET  /api/waitlist/patient/{id}
 *
 * NOTA: ms-waitlist aún no está implementado.
 *       Estas rutas retornarán 503 hasta que el microservicio esté disponible.
 */
@RestController
@RequestMapping("/api/waitlist")
public class WaitlistController {

    private final WaitlistService waitlistService;

    public WaitlistController(WaitlistService waitlistService) {
        this.waitlistService = waitlistService;
    }

    @GetMapping
    public ResponseEntity<List<WaitlistEntryResponse>> getAll() {
        return ResponseEntity.ok(waitlistService.getAll());
    }

    @PostMapping
    public ResponseEntity<WaitlistEntryResponse> enqueue(@Valid @RequestBody CreateWaitlistEntryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(waitlistService.enqueue(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WaitlistEntryResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(waitlistService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable String id) {
        waitlistService.remove(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<WaitlistEntryResponse>> getByPatient(@PathVariable String patientId) {
        return ResponseEntity.ok(waitlistService.getByPatient(patientId));
    }
}
