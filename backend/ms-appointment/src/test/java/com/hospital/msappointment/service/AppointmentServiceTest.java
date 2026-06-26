package com.hospital.msappointment.service;

import com.hospital.msappointment.dto.request.CreateAppointmentRequestDTO;
import com.hospital.msappointment.dto.request.UpdateAppointmentRequestDTO;
import com.hospital.msappointment.dto.response.AppointmentResponseDTO;
import com.hospital.msappointment.entity.Appointment;
import com.hospital.msappointment.entity.enums.AppointmentStatus;
import com.hospital.msappointment.entity.enums.AppointmentType;
import com.hospital.msappointment.entity.enums.CancelledBy;
import com.hospital.msappointment.entity.enums.Specialty;
import com.hospital.msappointment.exception.AppointmentConflictException;
import com.hospital.msappointment.exception.AppointmentNotFoundException;
import com.hospital.msappointment.repository.AppointmentRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para AppointmentService.
 * Verifica lógica de negocio (crear, cancelar, actualizar) sin base de datos.
 */
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private static final UUID PATIENT_ID = UUID.randomUUID();
    private static final UUID DOCTOR_ID  = UUID.randomUUID();

    private Appointment sampleAppointment() {
        return Appointment.builder()
                .id(UUID.randomUUID())
                .patientId(PATIENT_ID)
                .doctorId(DOCTOR_ID)
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .durationMinutes(30)
                .specialty(Specialty.CARDIOLOGY)
                .appointmentType(AppointmentType.CONSULTA)
                .status(AppointmentStatus.PENDING)
                .active(true)
                .build();
    }

    private CreateAppointmentRequestDTO sampleCreateRequest() {
        CreateAppointmentRequestDTO dto = new CreateAppointmentRequestDTO();
        dto.setPatientId(PATIENT_ID);
        dto.setDoctorId(DOCTOR_ID);
        dto.setScheduledAt(LocalDateTime.now().plusDays(1));
        dto.setDurationMinutes(30);
        dto.setSpecialty(Specialty.CARDIOLOGY);
        dto.setAppointmentType(AppointmentType.CONSULTA);
        return dto;
    }

    // ─── createAppointment ────────────────────────────────────────────────────

    @Test
    void createAppointment_happyPath_returnsResponse() {
        // El BFF valida existencia de paciente/doctor antes de llegar aquí.
        when(appointmentRepository.existsByDoctorIdAndScheduledAtBetweenAndActiveTrueAndStatusNot(
                any(), any(), any(), any())).thenReturn(false);

        Appointment saved = sampleAppointment();
        when(appointmentRepository.save(any())).thenReturn(saved);

        AppointmentResponseDTO result = appointmentService.createAppointment(sampleCreateRequest());

        assertThat(result.getPatientId()).isEqualTo(PATIENT_ID);
        assertThat(result.getDoctorId()).isEqualTo(DOCTOR_ID);
        assertThat(result.getSpecialty()).isEqualTo(Specialty.CARDIOLOGY);
        assertThat(result.getAppointmentType()).isEqualTo(AppointmentType.CONSULTA);
        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.PENDING);
        verify(appointmentRepository).save(any());
    }

    @Test
    void createAppointment_defaultsToConsultaWhenTypeNull() {
        when(appointmentRepository.existsByDoctorIdAndScheduledAtBetweenAndActiveTrueAndStatusNot(
                any(), any(), any(), any())).thenReturn(false);

        CreateAppointmentRequestDTO dto = sampleCreateRequest();
        dto.setAppointmentType(null); // null → debe defaultear a CONSULTA

        Appointment saved = sampleAppointment(); // ya tiene CONSULTA
        when(appointmentRepository.save(any())).thenReturn(saved);

        AppointmentResponseDTO result = appointmentService.createAppointment(dto);

        assertThat(result.getAppointmentType()).isEqualTo(AppointmentType.CONSULTA);
    }

    @Test
    void createAppointment_scheduleConflict_throwsConflictException() {
        when(appointmentRepository.existsByDoctorIdAndScheduledAtBetweenAndActiveTrueAndStatusNot(
                any(), any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> appointmentService.createAppointment(sampleCreateRequest()))
                .isInstanceOf(AppointmentConflictException.class)
                .hasMessageContaining("horario");

        verify(appointmentRepository, never()).save(any());
    }

    // ─── getAppointmentById ───────────────────────────────────────────────────

    @Test
    void getAppointmentById_found_returnsResponse() {
        Appointment appt = sampleAppointment();
        when(appointmentRepository.findByIdAndActiveTrue(appt.getId()))
                .thenReturn(Optional.of(appt));

        AppointmentResponseDTO result = appointmentService.getAppointmentById(appt.getId());

        assertThat(result.getId()).isEqualTo(appt.getId());
        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.PENDING);
    }

    @Test
    void getAppointmentById_notFound_throwsNotFoundException() {
        UUID id = UUID.randomUUID();
        when(appointmentRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.getAppointmentById(id))
                .isInstanceOf(AppointmentNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    // ─── getByPatient / getByDoctor ───────────────────────────────────────────

    @Test
    void getByPatient_returnsList() {
        when(appointmentRepository.findByPatientIdAndActiveTrue(PATIENT_ID))
                .thenReturn(List.of(sampleAppointment(), sampleAppointment()));

        assertThat(appointmentService.getByPatient(PATIENT_ID)).hasSize(2);
    }

    @Test
    void getByDoctor_returnsList() {
        when(appointmentRepository.findByDoctorIdAndActiveTrue(DOCTOR_ID))
                .thenReturn(List.of(sampleAppointment()));

        assertThat(appointmentService.getByDoctor(DOCTOR_ID)).hasSize(1);
    }

    // ─── updateAppointment ────────────────────────────────────────────────────

    @Test
    void updateAppointment_updatesStatus() {
        Appointment appt = sampleAppointment();
        when(appointmentRepository.findByIdAndActiveTrue(appt.getId()))
                .thenReturn(Optional.of(appt));
        when(appointmentRepository.save(any())).thenReturn(appt);

        UpdateAppointmentRequestDTO dto = new UpdateAppointmentRequestDTO();
        dto.setStatus(AppointmentStatus.CONFIRMED);

        AppointmentResponseDTO result = appointmentService.updateAppointment(appt.getId(), dto);

        assertThat(appt.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        verify(appointmentRepository).save(appt);
    }

    // ─── cancelByDoctor ───────────────────────────────────────────────────────

    @Test
    void cancelByDoctor_setsStatusCancelledAndCancelledByDoctor() {
        Appointment appt = sampleAppointment();
        when(appointmentRepository.findByIdAndActiveTrue(appt.getId()))
                .thenReturn(Optional.of(appt));
        when(appointmentRepository.save(any())).thenReturn(appt);

        AppointmentResponseDTO result = appointmentService.cancelByDoctor(appt.getId());

        assertThat(appt.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(appt.getCancelledBy()).isEqualTo(CancelledBy.DOCTOR);
        verify(appointmentRepository).save(appt);
    }

    @Test
    void cancelByDoctor_notFound_throwsNotFoundException() {
        UUID id = UUID.randomUUID();
        when(appointmentRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.cancelByDoctor(id))
                .isInstanceOf(AppointmentNotFoundException.class);
    }

    // ─── cancelByPatient ──────────────────────────────────────────────────────

    @Test
    void cancelByPatient_setsStatusCancelledAndCancelledByPatient() {
        Appointment appt = sampleAppointment();
        when(appointmentRepository.findByIdAndActiveTrue(appt.getId()))
                .thenReturn(Optional.of(appt));
        when(appointmentRepository.save(any())).thenReturn(appt);

        AppointmentResponseDTO result = appointmentService.cancelByPatient(appt.getId());

        assertThat(appt.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(appt.getCancelledBy()).isEqualTo(CancelledBy.PATIENT);
        verify(appointmentRepository).save(appt);
    }

    @Test
    void cancelByPatient_notFound_throwsNotFoundException() {
        UUID id = UUID.randomUUID();
        when(appointmentRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.cancelByPatient(id))
                .isInstanceOf(AppointmentNotFoundException.class);
    }

    // ─── cancelAppointment (genérico) ─────────────────────────────────────────

    @Test
    void cancelAppointment_setsStatusCancelledWithoutCancelledBy() {
        Appointment appt = sampleAppointment();
        when(appointmentRepository.findByIdAndActiveTrue(appt.getId()))
                .thenReturn(Optional.of(appt));
        when(appointmentRepository.save(any())).thenReturn(appt);

        appointmentService.cancelAppointment(appt.getId());

        assertThat(appt.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(appt.getCancelledBy()).isNull(); // genérico no establece cancelledBy
    }

    // ─── deleteAppointment (soft delete) ──────────────────────────────────────

    @Test
    void deleteAppointment_setsActiveFalse() {
        Appointment appt = sampleAppointment();
        when(appointmentRepository.findByIdAndActiveTrue(appt.getId()))
                .thenReturn(Optional.of(appt));
        when(appointmentRepository.save(any())).thenReturn(appt);

        appointmentService.deleteAppointment(appt.getId());

        assertThat(appt.getActive()).isFalse();
        verify(appointmentRepository).save(appt);
    }

    @Test
    void deleteAppointment_notFound_throwsNotFoundException() {
        UUID id = UUID.randomUUID();
        when(appointmentRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.deleteAppointment(id))
                .isInstanceOf(AppointmentNotFoundException.class);
    }
}
