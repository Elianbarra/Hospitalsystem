package cl.rednorte.bff.controller;

import cl.rednorte.bff.dto.request.CreateAppointmentRequestDTO;
import cl.rednorte.bff.dto.request.UpdateAppointmentRequestDTO;
import cl.rednorte.bff.dto.response.AppointmentResponseDTO;
import cl.rednorte.bff.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Citas médicas", description = "Creación y gestión de citas entre pacientes y médicos")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @Operation(summary = "Listar citas", description = "Devuelve todas las citas médicas registradas")
    @GetMapping
    public ResponseEntity<List<AppointmentResponseDTO>> getAll() {
        return ResponseEntity.ok(appointmentService.getAll());
    }

    @Operation(summary = "Crear cita", description = "Registra una nueva cita médica para un paciente con un médico")
    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> create(@Valid @RequestBody CreateAppointmentRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.create(request));
    }

    @Operation(summary = "Obtener cita", description = "Devuelve el detalle de una cita por su ID")
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(appointmentService.getById(id));
    }

    @Operation(summary = "Actualizar cita", description = "Modifica los datos de una cita existente")
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> update(
            @PathVariable String id,
            @RequestBody UpdateAppointmentRequestDTO request) {
        return ResponseEntity.ok(appointmentService.update(id, request));
    }

    @Operation(summary = "Cancelar cita", description = "Cancela y elimina una cita médica por su ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable String id) {
        appointmentService.cancel(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Citas por paciente", description = "Lista todas las citas de un paciente específico")
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponseDTO>> getByPatient(@PathVariable String patientId) {
        return ResponseEntity.ok(appointmentService.getByPatient(patientId));
    }

    @Operation(summary = "Citas por médico", description = "Lista todas las citas asignadas a un médico específico")
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentResponseDTO>> getByDoctor(@PathVariable String doctorId) {
        return ResponseEntity.ok(appointmentService.getByDoctor(doctorId));
    }
}
