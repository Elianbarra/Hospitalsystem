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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WaitlistServiceTest {

    @Mock
    private WaitlistRepository waitlistRepository;

    @InjectMocks
    private WaitlistService waitlistService;

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static final UUID PATIENT_ID = UUID.randomUUID();

    private WaitlistEntry sampleEntry() {
        return WaitlistEntry.builder()
                .id(UUID.randomUUID())
                .patientId(PATIENT_ID)
                .specialty(Specialty.MEDICINA_GENERAL)
                .priority(Priority.NORMAL)
                .status(WaitlistStatus.WAITING)
                .notes("Dolor de cabeza")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private CreateWaitlistEntryRequestDTO createDTO() {
        return CreateWaitlistEntryRequestDTO.builder()
                .patientId(PATIENT_ID)
                .specialty(Specialty.MEDICINA_GENERAL)
                .priority(Priority.NORMAL)
                .notes("Dolor de cabeza")
                .build();
    }

    // ── addToWaitlist ─────────────────────────────────────────────────────────

    @Test
    void addToWaitlist_happyPath_returnsResponse() {
        WaitlistEntry saved = sampleEntry();
        when(waitlistRepository.save(any())).thenReturn(saved);

        WaitlistEntryResponseDTO result = waitlistService.addToWaitlist(createDTO());

        assertThat(result.getPatientId()).isEqualTo(PATIENT_ID);
        assertThat(result.getSpecialty()).isEqualTo(Specialty.MEDICINA_GENERAL);
        assertThat(result.getPriority()).isEqualTo(Priority.NORMAL);
        assertThat(result.getStatus()).isEqualTo(WaitlistStatus.WAITING);
        verify(waitlistRepository).save(any());
    }

    @Test
    void addToWaitlist_withUrgentPriority_savesCorrectly() {
        CreateWaitlistEntryRequestDTO dto = CreateWaitlistEntryRequestDTO.builder()
                .patientId(PATIENT_ID)
                .specialty(Specialty.CARDIOLOGIA)
                .priority(Priority.URGENTE)
                .build();

        WaitlistEntry saved = WaitlistEntry.builder()
                .id(UUID.randomUUID())
                .patientId(PATIENT_ID)
                .specialty(Specialty.CARDIOLOGIA)
                .priority(Priority.URGENTE)
                .status(WaitlistStatus.WAITING)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(waitlistRepository.save(any())).thenReturn(saved);

        WaitlistEntryResponseDTO result = waitlistService.addToWaitlist(dto);

        assertThat(result.getPriority()).isEqualTo(Priority.URGENTE);
        assertThat(result.getSpecialty()).isEqualTo(Specialty.CARDIOLOGIA);
    }

    // ── getAll ────────────────────────────────────────────────────────────────

    @Test
    void getAll_returnsMappedList() {
        when(waitlistRepository.findByActiveTrueOrderByPriorityDescCreatedAtAsc())
                .thenReturn(List.of(sampleEntry(), sampleEntry()));

        List<WaitlistEntryResponseDTO> result = waitlistService.getAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void getAll_emptyRepo_returnsEmptyList() {
        when(waitlistRepository.findByActiveTrueOrderByPriorityDescCreatedAtAsc())
                .thenReturn(List.of());

        assertThat(waitlistService.getAll()).isEmpty();
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    void getById_found_returnsResponse() {
        WaitlistEntry entry = sampleEntry();
        when(waitlistRepository.findByIdAndActiveTrue(entry.getId()))
                .thenReturn(Optional.of(entry));

        WaitlistEntryResponseDTO result = waitlistService.getById(entry.getId());

        assertThat(result.getId()).isEqualTo(entry.getId());
        assertThat(result.getSpecialty()).isEqualTo(Specialty.MEDICINA_GENERAL);
    }

    @Test
    void getById_notFound_throwsWaitlistEntryNotFoundException() {
        UUID id = UUID.randomUUID();
        when(waitlistRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> waitlistService.getById(id))
                .isInstanceOf(WaitlistEntryNotFoundException.class);
    }

    // ── getByPatient ──────────────────────────────────────────────────────────

    @Test
    void getByPatient_returnsMappedList() {
        when(waitlistRepository.findByPatientIdAndActiveTrueOrderByCreatedAtAsc(PATIENT_ID))
                .thenReturn(List.of(sampleEntry()));

        List<WaitlistEntryResponseDTO> result = waitlistService.getByPatient(PATIENT_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPatientId()).isEqualTo(PATIENT_ID);
    }

    // ── getBySpecialty ────────────────────────────────────────────────────────

    @Test
    void getBySpecialty_returnsMappedList() {
        when(waitlistRepository.findBySpecialtyAndActiveTrueOrderByPriorityDescCreatedAtAsc(Specialty.MEDICINA_GENERAL))
                .thenReturn(List.of(sampleEntry()));

        List<WaitlistEntryResponseDTO> result = waitlistService.getBySpecialty(Specialty.MEDICINA_GENERAL);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSpecialty()).isEqualTo(Specialty.MEDICINA_GENERAL);
    }

    // ── getByStatus ───────────────────────────────────────────────────────────

    @Test
    void getByStatus_returnsOnlyMatchingEntries() {
        when(waitlistRepository.findByStatusAndActiveTrueOrderByPriorityDescCreatedAtAsc(WaitlistStatus.WAITING))
                .thenReturn(List.of(sampleEntry()));

        List<WaitlistEntryResponseDTO> result = waitlistService.getByStatus(WaitlistStatus.WAITING);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(WaitlistStatus.WAITING);
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void update_updatesOnlyProvidedFields() {
        WaitlistEntry entry = sampleEntry();
        when(waitlistRepository.findByIdAndActiveTrue(entry.getId()))
                .thenReturn(Optional.of(entry));
        when(waitlistRepository.save(any())).thenReturn(entry);

        UpdateWaitlistEntryRequestDTO dto = UpdateWaitlistEntryRequestDTO.builder()
                .priority(Priority.CRITICO)
                .build();

        waitlistService.update(entry.getId(), dto);

        assertThat(entry.getPriority()).isEqualTo(Priority.CRITICO);
        assertThat(entry.getStatus()).isEqualTo(WaitlistStatus.WAITING);   // sin cambio
        assertThat(entry.getNotes()).isEqualTo("Dolor de cabeza");         // sin cambio
        verify(waitlistRepository).save(entry);
    }

    @Test
    void update_notFound_throwsWaitlistEntryNotFoundException() {
        UUID id = UUID.randomUUID();
        when(waitlistRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

        UpdateWaitlistEntryRequestDTO dto = UpdateWaitlistEntryRequestDTO.builder()
                .status(WaitlistStatus.NOTIFIED)
                .build();

        assertThatThrownBy(() -> waitlistService.update(id, dto))
                .isInstanceOf(WaitlistEntryNotFoundException.class);
    }

    // ── cancel ────────────────────────────────────────────────────────────────

    @Test
    void cancel_setsStatusCancelled() {
        WaitlistEntry entry = sampleEntry();
        when(waitlistRepository.findByIdAndActiveTrue(entry.getId()))
                .thenReturn(Optional.of(entry));
        when(waitlistRepository.save(any())).thenReturn(entry);

        WaitlistEntryResponseDTO result = waitlistService.cancel(entry.getId());

        assertThat(entry.getStatus()).isEqualTo(WaitlistStatus.CANCELLED);
        verify(waitlistRepository).save(entry);
    }

    @Test
    void cancel_notFound_throwsWaitlistEntryNotFoundException() {
        UUID id = UUID.randomUUID();
        when(waitlistRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> waitlistService.cancel(id))
                .isInstanceOf(WaitlistEntryNotFoundException.class);
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_setsActiveFalse() {
        WaitlistEntry entry = sampleEntry();
        when(waitlistRepository.findByIdAndActiveTrue(entry.getId()))
                .thenReturn(Optional.of(entry));

        waitlistService.delete(entry.getId());

        assertThat(entry.getActive()).isFalse();
        verify(waitlistRepository).save(entry);
    }

    @Test
    void delete_notFound_throwsWaitlistEntryNotFoundException() {
        UUID id = UUID.randomUUID();
        when(waitlistRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> waitlistService.delete(id))
                .isInstanceOf(WaitlistEntryNotFoundException.class);
    }
}
