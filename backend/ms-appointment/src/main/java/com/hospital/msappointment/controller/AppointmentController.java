package com.hospital.msappointment.controller;

import com.hospital.msappointment.dto.request.CreateAppointmentRequestDTO;
import com.hospital.msappointment.dto.request.UpdateAppointmentRequestDTO;
import com.hospital.msappointment.dto.response.AppointmentResponseDTO;
import com.hospital.msappointment.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Gestión de citas médicas")
@SecurityRequirement(name = "bearerAuth")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Operation(summary = "Crear cita", description = "Registra una nueva cita médica para un paciente con un médico")
    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> create(
            @Valid @RequestBody CreateAppointmentRequestDTO dto) {
        return ResponseEntity.status(201).body(appointmentService.createAppointment(dto));
    }

    @Operation(summary = "Listar citas", description = "Devuelve todas las citas médicas registradas")
    @GetMapping
    public ResponseEntity<List<AppointmentResponseDTO>> getAll() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @Operation(summary = "Obtener cita", description = "Devuelve el detalle de una cita por su ID")
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    @Operation(summary = "Citas por paciente", description = "Lista todas las citas de un paciente específico")
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponseDTO>> getByPatient(@PathVariable UUID patientId) {
        return ResponseEntity.ok(appointmentService.getByPatient(patientId));
    }

    @Operation(summary = "Citas por médico", description = "Lista todas las citas asignadas a un médico específico")
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentResponseDTO>> getByDoctor(@PathVariable UUID doctorId) {
        return ResponseEntity.ok(appointmentService.getByDoctor(doctorId));
    }

    @Operation(summary = "Actualizar cita", description = "Modifica los datos de una cita existente")
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAppointmentRequestDTO dto) {
        return ResponseEntity.ok(appointmentService.updateAppointment(id, dto));
    }

    @Operation(summary = "Cancelar cita (genérico)", description = "Marca una cita como cancelada sin especificar quién cancela")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponseDTO> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(id));
    }

    @Operation(
        summary = "Cancelar cita — médico",
        description = "El médico cancela la cita. El BFF orquestará la reasignación automática al siguiente en lista de espera."
    )
    @PutMapping("/{id}/cancel-doctor")
    public ResponseEntity<AppointmentResponseDTO> cancelByDoctor(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.cancelByDoctor(id));
    }

    @Operation(
        summary = "Cancelar cita — paciente",
        description = "El paciente cancela su cita. El BFF orquestará el reencola al final de la lista de espera (requeueToEnd)."
    )
    @PutMapping("/{id}/cancel-patient")
    public ResponseEntity<AppointmentResponseDTO> cancelByPatient(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.cancelByPatient(id));
    }

    @Operation(summary = "Eliminar cita", description = "Elimina permanentemente una cita médica por su ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

    // TODO: remove — only for verifying GlitchTip integration
    @GetMapping("/test-sentry")
    public ResponseEntity<Void> testSentry() {
        throw new RuntimeException("GlitchTip test error from ms-appointment");
    }
}
