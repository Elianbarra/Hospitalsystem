package com.hospital.msappointment.service;

import com.hospital.msappointment.client.user.UserRestClient;
import com.hospital.msappointment.client.user.dto.UserResponseDTO;
import com.hospital.msappointment.dto.request.CreateAppointmentRequestDTO;
import com.hospital.msappointment.dto.request.UpdateAppointmentRequestDTO;
import com.hospital.msappointment.dto.response.AppointmentResponseDTO;
import com.hospital.msappointment.entity.Appointment;
import com.hospital.msappointment.entity.enums.AppointmentStatus;
import com.hospital.msappointment.entity.enums.Specialty;
import com.hospital.msappointment.exception.AppointmentConflictException;
import com.hospital.msappointment.exception.AppointmentNotFoundException;
import com.hospital.msappointment.exception.UserNotFoundException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private UserRestClient userRestClient;

    @InjectMocks
    private AppointmentService appointmentService;

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static final UUID PATIENT_ID = UUID.randomUUID();
    private static final UUID DOCTOR_ID  = UUID.randomUUID();

    private CreateAppointmentRequestDTO createDTO() {
        CreateAppointmentRequestDTO dto = new CreateAppointmentRequestDTO();
        dto.setPatientId(PATIENT_ID);
        dto.setDoctorId(DOCTOR_ID);
        dto.setScheduledAt(LocalDateTime.now().plusDays(1));
        dto.setDurationMinutes(30);
        dto.setSpecialty(Specialty.GENERAL);
        dto.setNotes("Revisión general");
        return dto;
    }

    private Appointment sampleAppointment() {
        return Appointment.builder()
                .id(UUID.randomUUID())
                .patientId(PATIENT_ID)
                .doctorId(DOCTOR_ID)
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .durationMinutes(30)
                .specialty(Specialty.GENERAL)
                .status(AppointmentStatus.PENDING)
                .notes("Revisión general")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ── createAppointment ─────────────────────────────────────────────────────

    @Test
    void createAppointment_happyPath_returnsResponse() {
        Appointment saved = sampleAppointment();
        when(userRestClient.getUserById(PATIENT_ID)).thenReturn(new UserResponseDTO());
        when(userRestClient.getUserById(DOCTOR_ID)).thenReturn(new UserResponseDTO());
        when(appointmentRepository
                .existsByDoctorIdAndScheduledAtBetweenAndActiveTrueAndStatusNot(
                        any(), any(), any(), any()))
                .thenReturn(false);
        when(appointmentRepository.save(any())).thenReturn(saved);

        AppointmentResponseDTO result = appointmentService.createAppointment(createDTO());

        assertThat(result.getPatientId()).isEqualTo(PATIENT_ID);
        assertThat(result.getDoctorId()).isEqualTo(DOCTOR_ID);
        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.PENDING);
        verify(appointmentRepository).save(any());
    }

    @Test
    void createAppointment_patientNotFound_throwsUserNotFoundException() {
        doThrow(new UserNotFoundException(PATIENT_ID))
                .when(userRestClient).getUserById(PATIENT_ID);

        assertThatThrownBy(() -> appointmentService.createAppointment(createDTO()))
                .isInstanceOf(UserNotFoundException.class);

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void createAppointment_doctorNotFound_throwsUserNotFoundException() {
        when(userRestClient.getUserById(PATIENT_ID)).thenReturn(new UserResponseDTO());
        doThrow(new UserNotFoundException(DOCTOR_ID))
                .when(userRestClient).getUserById(DOCTOR_ID);

        assertThatThrownBy(() -> appointmentService.createAppointment(createDTO()))
                .isInstanceOf(UserNotFoundException.class);

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void createAppointment_scheduleConflict_throwsAppointmentConflictException() {
        when(userRestClient.getUserById(any())).thenReturn(new UserResponseDTO());
        when(appointmentRepository
                .existsByDoctorIdAndScheduledAtBetweenAndActiveTrueAndStatusNot(
                        any(), any(), any(), any()))
                .thenReturn(true);

        assertThatThrownBy(() -> appointmentService.createAppointment(createDTO()))
                .isInstanceOf(AppointmentConflictException.class)
                .hasMessageContaining("horario");

        verify(appointmentRepository, never()).save(any());
    }

    // ── getAllAppointments ─────────────────────────────────────────────────────

    @Test
    void getAllAppointments_returnsMappedList() {
        when(appointmentRepository.findByActiveTrue())
                .thenReturn(List.of(sampleAppointment(), sampleAppointment()));

        List<AppointmentResponseDTO> result = appointmentService.getAllAppointments();

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(a -> a.getStatus() == AppointmentStatus.PENDING);
    }

    @Test
    void getAllAppointments_emptyRepo_returnsEmptyList() {
        when(appointmentRepository.findByActiveTrue()).thenReturn(List.of());

        assertThat(appointmentService.getAllAppointments()).isEmpty();
    }

    // ── getAppointmentById ────────────────────────────────────────────────────

    @Test
    void getAppointmentById_found_returnsResponse() {
        Appointment appt = sampleAppointment();
        when(appointmentRepository.findByIdAndActiveTrue(appt.getId()))
                .thenReturn(Optional.of(appt));

        AppointmentResponseDTO result = appointmentService.getAppointmentById(appt.getId());

        assertThat(result.getId()).isEqualTo(appt.getId());
        assertThat(result.getSpecialty()).isEqualTo(Specialty.GENERAL);
    }

    @Test
    void getAppointmentById_notFound_throwsAppointmentNotFoundException() {
        UUID id = UUID.randomUUID();
        when(appointmentRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.getAppointmentById(id))
                .isInstanceOf(AppointmentNotFoundException.class);
    }

    // ── getByPatient / getByDoctor ────────────────────────────────────────────

    @Test
    void getByPatient_returnsMappedList() {
        when(appointmentRepository.findByPatientIdAndActiveTrue(PATIENT_ID))
                .thenReturn(List.of(sampleAppointment()));

        List<AppointmentResponseDTO> result = appointmentService.getByPatient(PATIENT_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPatientId()).isEqualTo(PATIENT_ID);
    }

    @Test
    void getByDoctor_returnsMappedList() {
        when(appointmentRepository.findByDoctorIdAndActiveTrue(DOCTOR_ID))
                .thenReturn(List.of(sampleAppointment()));

        List<AppointmentResponseDTO> result = appointmentService.getByDoctor(DOCTOR_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDoctorId()).isEqualTo(DOCTOR_ID);
    }

    // ── updateAppointment ─────────────────────────────────────────────────────

    @Test
    void updateAppointment_updatesOnlyProvidedFields() {
        Appointment appt = sampleAppointment();
        when(appointmentRepository.findByIdAndActiveTrue(appt.getId()))
                .thenReturn(Optional.of(appt));
        when(appointmentRepository.save(any())).thenReturn(appt);

        UpdateAppointmentRequestDTO dto = new UpdateAppointmentRequestDTO();
        dto.setStatus(AppointmentStatus.CONFIRMED);

        appointmentService.updateAppointment(appt.getId(), dto);

        assertThat(appt.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(appt.getNotes()).isEqualTo("Revisión general"); // sin cambio
        verify(appointmentRepository).save(appt);
    }

    @Test
    void updateAppointment_notFound_throwsAppointmentNotFoundException() {
        UUID id = UUID.randomUUID();
        when(appointmentRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

        UpdateAppointmentRequestDTO dto = new UpdateAppointmentRequestDTO();
        dto.setStatus(AppointmentStatus.CONFIRMED);

        assertThatThrownBy(() -> appointmentService.updateAppointment(id, dto))
                .isInstanceOf(AppointmentNotFoundException.class);
    }

    // ── cancelAppointment ─────────────────────────────────────────────────────

    @Test
    void cancelAppointment_setsStatusCancelled() {
        Appointment appt = sampleAppointment();
        when(appointmentRepository.findByIdAndActiveTrue(appt.getId()))
                .thenReturn(Optional.of(appt));
        when(appointmentRepository.save(any())).thenReturn(appt);

        AppointmentResponseDTO result = appointmentService.cancelAppointment(appt.getId());

        assertThat(appt.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        verify(appointmentRepository).save(appt);
    }

    @Test
    void cancelAppointment_notFound_throwsAppointmentNotFoundException() {
        UUID id = UUID.randomUUID();
        when(appointmentRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.cancelAppointment(id))
                .isInstanceOf(AppointmentNotFoundException.class);
    }

    // ── deleteAppointment ─────────────────────────────────────────────────────

    @Test
    void deleteAppointment_setsActiveFalse() {
        Appointment appt = sampleAppointment();
        when(appointmentRepository.findByIdAndActiveTrue(appt.getId()))
                .thenReturn(Optional.of(appt));

        appointmentService.deleteAppointment(appt.getId());

        assertThat(appt.getActive()).isFalse();
        verify(appointmentRepository).save(appt);
    }

    @Test
    void deleteAppointment_notFound_throwsAppointmentNotFoundException() {
        UUID id = UUID.randomUUID();
        when(appointmentRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.deleteAppointment(id))
                .isInstanceOf(AppointmentNotFoundException.class);
    }
}
