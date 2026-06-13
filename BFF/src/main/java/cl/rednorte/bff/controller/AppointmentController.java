package cl.rednorte.bff.controller;

import cl.rednorte.bff.model.request.CreateAppointmentRequest;
import cl.rednorte.bff.model.request.UpdateAppointmentRequest;
import cl.rednorte.bff.model.response.AppointmentResponse;
import cl.rednorte.bff.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller MVC — capa Appointments (Citas médicas).
 *
 * Rutas expuestas por el BFF:
 *   GET    /api/appointments                     → ms-appointment GET  /api/appointments
 *   POST   /api/appointments                     → ms-appointment POST /api/appointments
 *   GET    /api/appointments/{id}                → ms-appointment GET  /api/appointments/{id}
 *   PUT    /api/appointments/{id}                → ms-appointment PUT  /api/appointments/{id}
 *   DELETE /api/appointments/{id}                → ms-appointment DELETE /api/appointments/{id}
 *   GET    /api/appointments/patient/{patientId} → ms-appointment GET  /api/appointments/patient/{id}
 *   GET    /api/appointments/doctor/{doctorId}   → ms-appointment GET  /api/appointments/doctor/{id}
 *
 * NOTA: ms-appointment aún no está implementado.
 *       Estas rutas retornarán 503 hasta que el microservicio esté disponible.
 */
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getAll() {
        return ResponseEntity.ok(appointmentService.getAll());
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody CreateAppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(appointmentService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponse> update(
            @PathVariable String id,
            @RequestBody UpdateAppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable String id) {
        appointmentService.cancel(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponse>> getByPatient(@PathVariable String patientId) {
        return ResponseEntity.ok(appointmentService.getByPatient(patientId));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentResponse>> getByDoctor(@PathVariable String doctorId) {
        return ResponseEntity.ok(appointmentService.getByDoctor(doctorId));
    }
}
