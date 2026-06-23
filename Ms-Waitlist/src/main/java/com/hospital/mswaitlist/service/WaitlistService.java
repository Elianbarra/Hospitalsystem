package com.hospital.mswaitlist.service;

import com.hospital.mswaitlist.dto.request.CreateWaitlistEntryRequestDTO;
import com.hospital.mswaitlist.dto.request.UpdateWaitlistEntryRequestDTO;
import com.hospital.mswaitlist.dto.response.WaitlistEntryResponseDTO;
import com.hospital.mswaitlist.entity.WaitlistEntry;
import com.hospital.mswaitlist.entity.enums.Priority;
import com.hospital.mswaitlist.entity.enums.Specialty;
import com.hospital.mswaitlist.entity.enums.WaitlistStatus;
import com.hospital.mswaitlist.exception.WaitlistEntryNotFoundException;
import com.hospital.mswaitlist.repository.WaitlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Capa de negocio para la lista de espera.
 * No se comunica con otros microservicios — el BFF orquesta toda la integración.
 *
 * Ordenamiento de cola (vitalRisk → priority → requeuedAt):
 *   1. vitalRisk = true primero (riesgo vital severo sube al tope absoluto)
 *   2. Prioridad: CRITICO > URGENTE > NORMAL
 *   3. requeuedAt ASC (más antiguo primero; se actualiza si paciente cancela y re-entra)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WaitlistService {

    private final WaitlistRepository waitlistRepository;

    // ─── Comparador de cola ───────────────────────────────────────────────────

    private static final Comparator<WaitlistEntry> QUEUE_ORDER =
        Comparator.comparing(WaitlistEntry::getVitalRisk).reversed()           // true primero
            .thenComparingInt(e -> priorityOrder(e.getPriority()))              // CRITICO=0, URGENTE=1, NORMAL=2
            .thenComparing(WaitlistEntry::getRequeuedAt);                       // más antiguo primero

    private static int priorityOrder(Priority p) {
        return switch (p) {
            case CRITICO -> 0;
            case URGENTE -> 1;
            case NORMAL  -> 2;
        };
    }

    // ─── Create ──────────────────────────────────────────────────────────────

    public WaitlistEntryResponseDTO addToWaitlist(CreateWaitlistEntryRequestDTO dto) {
        LocalDateTime now = LocalDateTime.now();
        WaitlistEntry entry = WaitlistEntry.builder()
                .patientId(dto.getPatientId())
                .specialty(dto.getSpecialty())
                .appointmentType(dto.getAppointmentType())
                .priority(dto.getPriority())
                .vitalRisk(dto.getVitalRisk() != null ? dto.getVitalRisk() : false)
                .notes(dto.getNotes())
                .requeuedAt(now)
                .build();

        WaitlistEntry saved = waitlistRepository.save(entry);
        log.info("Paciente {} agregado a lista de espera — especialidad: {}, tipo: {}, prioridad: {}, vitalRisk: {}",
                dto.getPatientId(), dto.getSpecialty(), dto.getAppointmentType(), dto.getPriority(), dto.getVitalRisk());
        return toResponse(saved);
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    public List<WaitlistEntryResponseDTO> getAll() {
        return waitlistRepository.findByActiveTrue()
                .stream()
                .sorted(QUEUE_ORDER)
                .map(this::toResponse)
                .toList();
    }

    public WaitlistEntryResponseDTO getById(UUID id) {
        return waitlistRepository.findByIdAndActiveTrue(id)
                .map(this::toResponse)
                .orElseThrow(() -> new WaitlistEntryNotFoundException(id));
    }

    public List<WaitlistEntryResponseDTO> getByPatient(UUID patientId) {
        return waitlistRepository.findByPatientIdAndActiveTrueOrderByRequeuedAtAsc(patientId)
                .stream().map(this::toResponse).toList();
    }

    public List<WaitlistEntryResponseDTO> getBySpecialty(Specialty specialty) {
        return waitlistRepository.findBySpecialtyAndActiveTrue(specialty)
                .stream()
                .sorted(QUEUE_ORDER)
                .map(this::toResponse)
                .toList();
    }

    public List<WaitlistEntryResponseDTO> getByStatus(WaitlistStatus status) {
        return waitlistRepository.findByStatusAndActiveTrue(status)
                .stream()
                .sorted(QUEUE_ORDER)
                .map(this::toResponse)
                .toList();
    }

    /**
     * Devuelve el siguiente paciente en cola para una especialidad.
     * Solo considera entradas con status WAITING.
     * Aplica ordenamiento: vitalRisk → priority → requeuedAt.
     */
    public Optional<WaitlistEntryResponseDTO> getNextForSpecialty(Specialty specialty) {
        return waitlistRepository.findBySpecialtyAndActiveTrue(specialty)
                .stream()
                .filter(e -> e.getStatus() == WaitlistStatus.WAITING)
                .min(QUEUE_ORDER)
                .map(this::toResponse);
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    public WaitlistEntryResponseDTO update(UUID id, UpdateWaitlistEntryRequestDTO dto) {
        WaitlistEntry entry = waitlistRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new WaitlistEntryNotFoundException(id));

        if (dto.getStatus() != null)    entry.setStatus(dto.getStatus());
        if (dto.getPriority() != null)  entry.setPriority(dto.getPriority());
        if (dto.getVitalRisk() != null) {
            entry.setVitalRisk(dto.getVitalRisk());
            log.info("Riesgo vital actualizado para entrada {} → vitalRisk={}", id, dto.getVitalRisk());
        }
        if (dto.getNotes() != null)     entry.setNotes(dto.getNotes());

        return toResponse(waitlistRepository.save(entry));
    }

    /**
     * Mueve al paciente al final de la cola actualizando requeuedAt a ahora.
     * Se usa cuando el paciente cancela su cita — pierde su posición
     * pero no sale de la lista de espera.
     */
    public WaitlistEntryResponseDTO requeueToEnd(UUID id) {
        WaitlistEntry entry = waitlistRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new WaitlistEntryNotFoundException(id));
        entry.setRequeuedAt(LocalDateTime.now());
        entry.setStatus(WaitlistStatus.WAITING);
        log.info("Paciente {} reubicado al final de la cola en especialidad {}", entry.getPatientId(), entry.getSpecialty());
        return toResponse(waitlistRepository.save(entry));
    }

    // ─── Cancel / Delete ──────────────────────────────────────────────────────

    public WaitlistEntryResponseDTO cancel(UUID id) {
        WaitlistEntry entry = waitlistRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new WaitlistEntryNotFoundException(id));
        entry.setStatus(WaitlistStatus.CANCELLED);
        return toResponse(waitlistRepository.save(entry));
    }

    public void delete(UUID id) {
        WaitlistEntry entry = waitlistRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new WaitlistEntryNotFoundException(id));
        entry.setActive(false);
        waitlistRepository.save(entry);
        log.info("Entrada {} eliminada de lista de espera (soft delete)", id);
    }

    // ─── Mapper ───────────────────────────────────────────────────────────────

    private WaitlistEntryResponseDTO toResponse(WaitlistEntry e) {
        return WaitlistEntryResponseDTO.builder()
                .id(e.getId())
                .patientId(e.getPatientId())
                .specialty(e.getSpecialty())
                .appointmentType(e.getAppointmentType())
                .priority(e.getPriority())
                .vitalRisk(e.getVitalRisk())
                .status(e.getStatus())
                .notes(e.getNotes())
                .createdAt(e.getCreatedAt())
                .requeuedAt(e.getRequeuedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
