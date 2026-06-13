package com.hospital.msappointment.controller;

import com.hospital.msappointment.client.AppointmentClient;
import com.hospital.msappointment.dto.request.CreateAppointmentRequestDTO;
import com.hospital.msappointment.dto.request.UpdateAppointmentRequestDTO;
import com.hospital.msappointment.dto.response.AppointmentResponseDTO;
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

    private final AppointmentClient appointmentClient;

    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> create(
            @Valid @RequestBody CreateAppointmentRequestDTO dto) {
        return ResponseEntity.status(201).body(appointmentClient.createAppointment(dto));
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponseDTO>> getAll() {
        return ResponseEntity.ok(appointmentClient.getAllAppointments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentClient.getAppointmentById(id));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponseDTO>> getByPatient(@PathVariable UUID patientId) {
        return ResponseEntity.ok(appointmentClient.getByPatient(patientId));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentResponseDTO>> getByDoctor(@PathVariable UUID doctorId) {
        return ResponseEntity.ok(appointmentClient.getByDoctor(doctorId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAppointmentRequestDTO dto) {
        return ResponseEntity.ok(appointmentClient.updateAppointment(id, dto));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponseDTO> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentClient.cancelAppointment(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        appointmentClient.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }
}
