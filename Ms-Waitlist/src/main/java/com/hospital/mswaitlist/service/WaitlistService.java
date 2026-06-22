package com.hospital.mswaitlist.service;

import com.hospital.mswaitlist.dto.request.CreateWaitlistEntryRequestDTO;
import com.hospital.mswaitlist.dto.request.UpdateWaitlistEntryRequestDTO;
import com.hospital.mswaitlist.dto.response.WaitlistEntryResponseDTO;
import com.hospital.mswaitlist.entity.WaitlistEntry;
import com.hospital.mswaitlist.entity.enums.Specialty;
import com.hospital.mswaitlist.entity.enums.WaitlistStatus;
import com.hospital.mswaitlist.exception.WaitlistEntryNotFoundException;
import com.hospital.mswaitlist.repository.WaitlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Capa de negocio para la lista de espera.
 * No se comunica con otros microservicios — el BFF orquesta toda la integración.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WaitlistService {

    private final WaitlistRepository waitlistRepository;

    // ─── Create ──────────────────────────────────────────────────────────────

    public WaitlistEntryResponseDTO addToWaitlist(CreateWaitlistEntryRequestDTO dto) {
        WaitlistEntry entry = WaitlistEntry.builder()
                .patientId(dto.getPatientId())
                .specialty(dto.getSpecialty())
                .priority(dto.getPriority())
                .notes(dto.getNotes())
                .build();

        WaitlistEntry saved = waitlistRepository.save(entry);
        log.info("Paciente {} agregado a lista de espera - especialidad: {}", dto.getPatientId(), dto.getSpecialty());
        return toResponse(saved);
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    public List<WaitlistEntryResponseDTO> getAll() {
        return waitlistRepository.findByActiveTrueOrderByPriorityDescCreatedAtAsc()
                .stream().map(this::toResponse).toList();
    }

    public WaitlistEntryResponseDTO getById(UUID id) {
        return waitlistRepository.findByIdAndActiveTrue(id)
                .map(this::toResponse)
                .orElseThrow(() -> new WaitlistEntryNotFoundException(id));
    }

    public List<WaitlistEntryResponseDTO> getByPatient(UUID patientId) {
        return waitlistRepository.findByPatientIdAndActiveTrueOrderByCreatedAtAsc(patientId)
                .stream().map(this::toResponse).toList();
    }

    public List<WaitlistEntryResponseDTO> getBySpecialty(Specialty specialty) {
        return waitlistRepository.findBySpecialtyAndActiveTrueOrderByPriorityDescCreatedAtAsc(specialty)
                .stream().map(this::toResponse).toList();
    }

    public List<WaitlistEntryResponseDTO> getByStatus(WaitlistStatus status) {
        return waitlistRepository.findByStatusAndActiveTrueOrderByPriorityDescCreatedAtAsc(status)
                .stream().map(this::toResponse).toList();
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    public WaitlistEntryResponseDTO update(UUID id, UpdateWaitlistEntryRequestDTO dto) {
        WaitlistEntry entry = waitlistRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new WaitlistEntryNotFoundException(id));

        if (dto.getStatus() != null)   entry.setStatus(dto.getStatus());
        if (dto.getPriority() != null) entry.setPriority(dto.getPriority());
        if (dto.getNotes() != null)    entry.setNotes(dto.getNotes());

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
                .priority(e.getPriority())
                .status(e.getStatus())
                .notes(e.getNotes())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
