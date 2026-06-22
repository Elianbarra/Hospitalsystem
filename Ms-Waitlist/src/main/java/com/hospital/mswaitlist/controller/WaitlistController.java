package com.hospital.mswaitlist.controller;

import com.hospital.mswaitlist.dto.request.CreateWaitlistEntryRequestDTO;
import com.hospital.mswaitlist.dto.request.UpdateWaitlistEntryRequestDTO;
import com.hospital.mswaitlist.dto.response.WaitlistEntryResponseDTO;
import com.hospital.mswaitlist.entity.enums.Specialty;
import com.hospital.mswaitlist.entity.enums.WaitlistStatus;
import com.hospital.mswaitlist.service.WaitlistService;
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
@RequestMapping("/api/waitlist")
@RequiredArgsConstructor
@Tag(name = "Waitlist", description = "Gestión de lista de espera por especialidad")
@SecurityRequirement(name = "bearerAuth")
public class WaitlistController {

    private final WaitlistService waitlistService;

    @Operation(summary = "Agregar a lista de espera", description = "Ingresa un paciente a la lista de espera de una especialidad")
    @PostMapping
    public ResponseEntity<WaitlistEntryResponseDTO> add(
            @Valid @RequestBody CreateWaitlistEntryRequestDTO dto) {
        return ResponseEntity.status(201).body(waitlistService.addToWaitlist(dto));
    }

    @Operation(summary = "Listar lista de espera", description = "Devuelve todas las entradas de la lista de espera")
    @GetMapping
    public ResponseEntity<List<WaitlistEntryResponseDTO>> getAll() {
        return ResponseEntity.ok(waitlistService.getAll());
    }

    @Operation(summary = "Obtener entrada", description = "Devuelve una entrada de la lista de espera por su ID")
    @GetMapping("/{id}")
    public ResponseEntity<WaitlistEntryResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(waitlistService.getById(id));
    }

    @Operation(summary = "Lista de espera por paciente", description = "Devuelve las entradas de lista de espera de un paciente específico")
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<WaitlistEntryResponseDTO>> getByPatient(@PathVariable UUID patientId) {
        return ResponseEntity.ok(waitlistService.getByPatient(patientId));
    }

    @Operation(summary = "Lista de espera por especialidad", description = "Filtra la lista de espera por especialidad médica")
    @GetMapping("/specialty/{specialty}")
    public ResponseEntity<List<WaitlistEntryResponseDTO>> getBySpecialty(@PathVariable Specialty specialty) {
        return ResponseEntity.ok(waitlistService.getBySpecialty(specialty));
    }

    @Operation(summary = "Lista de espera por estado", description = "Filtra la lista de espera por estado (WAITING, ATTENDED, CANCELLED)")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<WaitlistEntryResponseDTO>> getByStatus(@PathVariable WaitlistStatus status) {
        return ResponseEntity.ok(waitlistService.getByStatus(status));
    }

    @Operation(summary = "Actualizar entrada", description = "Modifica los datos de una entrada en la lista de espera")
    @PutMapping("/{id}")
    public ResponseEntity<WaitlistEntryResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWaitlistEntryRequestDTO dto) {
        return ResponseEntity.ok(waitlistService.update(id, dto));
    }

    @Operation(summary = "Cancelar entrada", description = "Marca como cancelada la entrada de un paciente en lista de espera")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<WaitlistEntryResponseDTO> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(waitlistService.cancel(id));
    }

    @Operation(summary = "Eliminar entrada", description = "Elimina permanentemente una entrada de la lista de espera")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        waitlistService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
