package com.hospital.mswaitlist.service;

import com.hospital.mswaitlist.dto.request.CreateWaitlistEntryRequestDTO;
import com.hospital.mswaitlist.dto.request.UpdateWaitlistEntryRequestDTO;
import com.hospital.mswaitlist.dto.response.WaitlistEntryResponseDTO;
import com.hospital.mswaitlist.entity.WaitlistEntry;
import com.hospital.mswaitlist.entity.enums.AppointmentType;
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

/**
 * Tests unitarios para WaitlistService.
 *
 * Ordenamiento de cola verificado (vitalRisk DESC → priority → requeuedAt ASC):
 *   vitalRisk=true sube al tope; luego CRITICO > URGENTE > NORMAL;
 *   en igualdad de prioridad, el más antiguo (requeuedAt menor) va primero.
 */
@ExtendWith(MockitoExtension.class)
class WaitlistServiceTest {

    @Mock
    private WaitlistRepository waitlistRepository;

    @InjectMocks
    private WaitlistService waitlistService;

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private WaitlistEntry entry(Priority priority, boolean vitalRisk, LocalDateTime requeuedAt) {
        return WaitlistEntry.builder()
                .id(UUID.randomUUID())
                .patientId(UUID.randomUUID())
                .specialty(Specialty.CARDIOLOGIA)
                .appointmentType(AppointmentType.CONSULTA)
                .priority(priority)
                .vitalRisk(vitalRisk)
                .status(WaitlistStatus.WAITING)
                .requeuedAt(requeuedAt)
                .active(true)
                .build();
    }

    private CreateWaitlistEntryRequestDTO sampleCreateDTO() {
        return CreateWaitlistEntryRequestDTO.builder()
                .patientId(UUID.randomUUID())
                .specialty(Specialty.CARDIOLOGIA)
                .appointmentType(AppointmentType.CONSULTA)
                .priority(Priority.NORMAL)
                .vitalRisk(false)
                .build();
    }

    // ─── addToWaitlist ────────────────────────────────────────────────────────

    @Test
    void addToWaitlist_setsDefaultsAndPersists() {
        WaitlistEntry saved = entry(Priority.NORMAL, false, LocalDateTime.now());
        when(waitlistRepository.save(any())).thenReturn(saved);

        WaitlistEntryResponseDTO result = waitlistService.addToWaitlist(sampleCreateDTO());

        assertThat(result.getSpecialty()).isEqualTo(Specialty.CARDIOLOGIA);
        assertThat(result.getPriority()).isEqualTo(Priority.NORMAL);
        assertThat(result.getVitalRisk()).isFalse();
        assertThat(result.getStatus()).isEqualTo(WaitlistStatus.WAITING);
        verify(waitlistRepository).save(any());
    }

    @Test
    void addToWaitlist_withVitalRisk_persistsVitalRiskTrue() {
        CreateWaitlistEntryRequestDTO dto = sampleCreateDTO();
        dto.setVitalRisk(true);
        dto.setPriority(Priority.CRITICO);

        WaitlistEntry saved = entry(Priority.CRITICO, true, LocalDateTime.now());
        when(waitlistRepository.save(any())).thenReturn(saved);

        WaitlistEntryResponseDTO result = waitlistService.addToWaitlist(dto);

        assertThat(result.getVitalRisk()).isTrue();
        assertThat(result.getPriority()).isEqualTo(Priority.CRITICO);
    }

    @Test
    void addToWaitlist_nullVitalRisk_defaultsFalse() {
        CreateWaitlistEntryRequestDTO dto = sampleCreateDTO();
        dto.setVitalRisk(null); // null → service debe defaultear a false

        WaitlistEntry saved = entry(Priority.NORMAL, false, LocalDateTime.now());
        when(waitlistRepository.save(any())).thenReturn(saved);

        waitlistService.addToWaitlist(dto);

        // verificar que el objeto persistido tiene vitalRisk=false
        verify(waitlistRepository).save(argThat(e -> !e.getVitalRisk()));
    }

    // ─── getNextForSpecialty — ordenamiento ───────────────────────────────────

    @Test
    void getNextForSpecialty_vitalRiskBeatsHighPriority() {
        LocalDateTime t = LocalDateTime.now();
        WaitlistEntry critico  = entry(Priority.CRITICO, false, t.minusHours(1));
        WaitlistEntry vitalRisk = entry(Priority.NORMAL, true,  t); // vitalRisk=true, prioridad baja

        when(waitlistRepository.findBySpecialtyAndActiveTrue(Specialty.CARDIOLOGIA))
                .thenReturn(List.of(critico, vitalRisk));

        WaitlistEntryResponseDTO next = waitlistService.getNextForSpecialty(Specialty.CARDIOLOGIA)
                .orElseThrow();

        // vitalRisk=true debe ir primero, aunque su prioridad sea NORMAL
        assertThat(next.getId()).isEqualTo(vitalRisk.getId());
    }

    @Test
    void getNextForSpecialty_criticoBeatsUrgente() {
        LocalDateTime t = LocalDateTime.now();
        WaitlistEntry urgente = entry(Priority.URGENTE, false, t.minusHours(2));
        WaitlistEntry critico = entry(Priority.CRITICO, false, t);

        when(waitlistRepository.findBySpecialtyAndActiveTrue(Specialty.CARDIOLOGIA))
                .thenReturn(List.of(urgente, critico));

        WaitlistEntryResponseDTO next = waitlistService.getNextForSpecialty(Specialty.CARDIOLOGIA)
                .orElseThrow();

        assertThat(next.getId()).isEqualTo(critico.getId());
    }

    @Test
    void getNextForSpecialty_samePriority_oldestRequeuedAtFirst() {
        LocalDateTime now = LocalDateTime.now();
        WaitlistEntry newer = entry(Priority.NORMAL, false, now.minusHours(1));
        WaitlistEntry older  = entry(Priority.NORMAL, false, now.minusHours(3));

        when(waitlistRepository.findBySpecialtyAndActiveTrue(Specialty.CARDIOLOGIA))
                .thenReturn(List.of(newer, older));

        WaitlistEntryResponseDTO next = waitlistService.getNextForSpecialty(Specialty.CARDIOLOGIA)
                .orElseThrow();

        // El más antiguo (requeuedAt más pequeño) debe ir primero
        assertThat(next.getId()).isEqualTo(older.getId());
    }

    @Test
    void getNextForSpecialty_skipsNonWaitingEntries() {
        LocalDateTime t = LocalDateTime.now();
        WaitlistEntry offered   = entry(Priority.CRITICO, false, t.minusHours(5));
        offered.setStatus(WaitlistStatus.OFFERED); // no es WAITING — debe ignorarse

        WaitlistEntry waiting = entry(Priority.NORMAL, false, t);

        when(waitlistRepository.findBySpecialtyAndActiveTrue(Specialty.CARDIOLOGIA))
                .thenReturn(List.of(offered, waiting));

        WaitlistEntryResponseDTO next = waitlistService.getNextForSpecialty(Specialty.CARDIOLOGIA)
                .orElseThrow();

        // El OFFERED no cuenta; el WAITING (aunque sea NORMAL) es el único candidato
        assertThat(next.getId()).isEqualTo(waiting.getId());
    }

    @Test
    void getNextForSpecialty_noWaitingEntries_returnsEmpty() {
        WaitlistEntry offered = entry(Priority.CRITICO, false, LocalDateTime.now());
        offered.setStatus(WaitlistStatus.OFFERED);

        when(waitlistRepository.findBySpecialtyAndActiveTrue(Specialty.CARDIOLOGIA))
                .thenReturn(List.of(offered));

        assertThat(waitlistService.getNextForSpecialty(Specialty.CARDIOLOGIA)).isEmpty();
    }

    @Test
    void getNextForSpecialty_emptyQueue_returnsEmpty() {
        when(waitlistRepository.findBySpecialtyAndActiveTrue(Specialty.CARDIOLOGIA))
                .thenReturn(List.of());

        assertThat(waitlistService.getNextForSpecialty(Specialty.CARDIOLOGIA)).isEmpty();
    }

    // ─── getAll — ordenamiento ────────────────────────────────────────────────

    @Test
    void getAll_returnsEntriesSortedByQueueOrder() {
        LocalDateTime t = LocalDateTime.now();
        WaitlistEntry last   = entry(Priority.NORMAL,  false, t.minusHours(1));
        WaitlistEntry second = entry(Priority.URGENTE, false, t.minusHours(2));
        WaitlistEntry first  = entry(Priority.NORMAL,  true,  t); // vitalRisk=true → tope

        when(waitlistRepository.findByActiveTrue())
                .thenReturn(List.of(last, second, first)); // orden aleatorio

        List<WaitlistEntryResponseDTO> result = waitlistService.getAll();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getId()).isEqualTo(first.getId());  // vitalRisk=true
        assertThat(result.get(1).getId()).isEqualTo(second.getId()); // URGENTE
        assertThat(result.get(2).getId()).isEqualTo(last.getId());   // NORMAL
    }

    // ─── getById ──────────────────────────────────────────────────────────────

    @Test
    void getById_found_returnsResponse() {
        WaitlistEntry e = entry(Priority.NORMAL, false, LocalDateTime.now());
        when(waitlistRepository.findByIdAndActiveTrue(e.getId())).thenReturn(Optional.of(e));

        WaitlistEntryResponseDTO result = waitlistService.getById(e.getId());

        assertThat(result.getId()).isEqualTo(e.getId());
        assertThat(result.getSpecialty()).isEqualTo(Specialty.CARDIOLOGIA);
    }

    @Test
    void getById_notFound_throwsWaitlistEntryNotFoundException() {
        UUID id = UUID.randomUUID();
        when(waitlistRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> waitlistService.getById(id))
                .isInstanceOf(WaitlistEntryNotFoundException.class);
    }

    // ─── requeueToEnd ─────────────────────────────────────────────────────────

    @Test
    void requeueToEnd_updatesRequeuedAtAndSetsWaiting() {
        WaitlistEntry entry = entry(Priority.NORMAL, false,
                LocalDateTime.now().minusDays(2)); // posición antigua
        entry.setStatus(WaitlistStatus.OFFERED);   // suponer que estaba en OFFERED
        when(waitlistRepository.findByIdAndActiveTrue(entry.getId()))
                .thenReturn(Optional.of(entry));
        when(waitlistRepository.save(any())).thenReturn(entry);

        LocalDateTime beforeCall = LocalDateTime.now();
        waitlistService.requeueToEnd(entry.getId());
        LocalDateTime afterCall  = LocalDateTime.now();

        // requeuedAt debe haberse actualizado a un momento cercano a la llamada
        assertThat(entry.getRequeuedAt()).isAfterOrEqualTo(beforeCall);
        assertThat(entry.getRequeuedAt()).isBeforeOrEqualTo(afterCall);
        assertThat(entry.getStatus()).isEqualTo(WaitlistStatus.WAITING);
        verify(waitlistRepository).save(entry);
    }

    @Test
    void requeueToEnd_notFound_throwsNotFoundException() {
        UUID id = UUID.randomUUID();
        when(waitlistRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> waitlistService.requeueToEnd(id))
                .isInstanceOf(WaitlistEntryNotFoundException.class);
    }

    // ─── update ───────────────────────────────────────────────────────────────

    @Test
    void update_vitalRisk_updatesFlag() {
        WaitlistEntry e = entry(Priority.NORMAL, false, LocalDateTime.now());
        when(waitlistRepository.findByIdAndActiveTrue(e.getId())).thenReturn(Optional.of(e));
        when(waitlistRepository.save(any())).thenReturn(e);

        UpdateWaitlistEntryRequestDTO dto = new UpdateWaitlistEntryRequestDTO();
        dto.setVitalRisk(true);

        waitlistService.update(e.getId(), dto);

        assertThat(e.getVitalRisk()).isTrue();
        verify(waitlistRepository).save(e);
    }

    @Test
    void update_status_updatesStatus() {
        WaitlistEntry e = entry(Priority.URGENTE, false, LocalDateTime.now());
        when(waitlistRepository.findByIdAndActiveTrue(e.getId())).thenReturn(Optional.of(e));
        when(waitlistRepository.save(any())).thenReturn(e);

        UpdateWaitlistEntryRequestDTO dto = new UpdateWaitlistEntryRequestDTO();
        dto.setStatus(WaitlistStatus.OFFERED);

        waitlistService.update(e.getId(), dto);

        assertThat(e.getStatus()).isEqualTo(WaitlistStatus.OFFERED);
    }

    // ─── cancel ───────────────────────────────────────────────────────────────

    @Test
    void cancel_setsStatusCancelled() {
        WaitlistEntry e = entry(Priority.NORMAL, false, LocalDateTime.now());
        when(waitlistRepository.findByIdAndActiveTrue(e.getId())).thenReturn(Optional.of(e));
        when(waitlistRepository.save(any())).thenReturn(e);

        waitlistService.cancel(e.getId());

        assertThat(e.getStatus()).isEqualTo(WaitlistStatus.CANCELLED);
        verify(waitlistRepository).save(e);
    }

    // ─── delete (soft delete) ─────────────────────────────────────────────────

    @Test
    void delete_setsActiveFalse() {
        WaitlistEntry e = entry(Priority.NORMAL, false, LocalDateTime.now());
        when(waitlistRepository.findByIdAndActiveTrue(e.getId())).thenReturn(Optional.of(e));
        when(waitlistRepository.save(any())).thenReturn(e);

        waitlistService.delete(e.getId());

        assertThat(e.getActive()).isFalse();
        verify(waitlistRepository).save(e);
    }

    @Test
    void delete_notFound_throwsNotFoundException() {
        UUID id = UUID.randomUUID();
        when(waitlistRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> waitlistService.delete(id))
                .isInstanceOf(WaitlistEntryNotFoundException.class);
    }
}
