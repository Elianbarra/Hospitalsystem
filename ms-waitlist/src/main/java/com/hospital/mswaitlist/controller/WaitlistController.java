package com.hospital.mswaitlist.controller;

import com.hospital.mswaitlist.dto.request.CreateWaitlistEntryRequestDTO;
import com.hospital.mswaitlist.dto.request.UpdateWaitlistEntryRequestDTO;
import com.hospital.mswaitlist.dto.response.WaitlistEntryResponseDTO;
import com.hospital.mswaitlist.entity.enums.Specialty;
import com.hospital.mswaitlist.entity.enums.WaitlistStatus;
import com.hospital.mswaitlist.service.WaitlistService;
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

    @PostMapping
    public ResponseEntity<WaitlistEntryResponseDTO> add(
            @Valid @RequestBody CreateWaitlistEntryRequestDTO dto) {
        return ResponseEntity.status(201).body(waitlistService.addToWaitlist(dto));
    }

    @GetMapping
    public ResponseEntity<List<WaitlistEntryResponseDTO>> getAll() {
        return ResponseEntity.ok(waitlistService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WaitlistEntryResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(waitlistService.getById(id));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<WaitlistEntryResponseDTO>> getByPatient(@PathVariable UUID patientId) {
        return ResponseEntity.ok(waitlistService.getByPatient(patientId));
    }

    @GetMapping("/specialty/{specialty}")
    public ResponseEntity<List<WaitlistEntryResponseDTO>> getBySpecialty(@PathVariable Specialty specialty) {
        return ResponseEntity.ok(waitlistService.getBySpecialty(specialty));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<WaitlistEntryResponseDTO>> getByStatus(@PathVariable WaitlistStatus status) {
        return ResponseEntity.ok(waitlistService.getByStatus(status));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WaitlistEntryResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWaitlistEntryRequestDTO dto) {
        return ResponseEntity.ok(waitlistService.update(id, dto));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<WaitlistEntryResponseDTO> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(waitlistService.cancel(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        waitlistService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
