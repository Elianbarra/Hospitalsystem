package com.hospital.msappointment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hospital.msappointment.dto.request.CreateAppointmentRequestDTO;
import com.hospital.msappointment.dto.request.UpdateAppointmentRequestDTO;
import com.hospital.msappointment.dto.response.AppointmentResponseDTO;
import com.hospital.msappointment.entity.enums.AppointmentStatus;
import com.hospital.msappointment.entity.enums.Specialty;
import com.hospital.msappointment.exception.AppointmentConflictException;
import com.hospital.msappointment.exception.AppointmentNotFoundException;
import com.hospital.msappointment.exception.GlobalExceptionHandler;
import com.hospital.msappointment.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppointmentController.class)
@Import(GlobalExceptionHandler.class)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentService appointmentService;

    // Evita que el auto-configure intente conectar al JWKS endpoint real
    @MockBean
    private JwtDecoder jwtDecoder;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static final UUID PATIENT_ID = UUID.randomUUID();
    private static final UUID DOCTOR_ID  = UUID.randomUUID();

    private AppointmentResponseDTO sampleResponse() {
        return AppointmentResponseDTO.builder()
                .id(UUID.randomUUID())
                .patientId(PATIENT_ID)
                .doctorId(DOCTOR_ID)
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .durationMinutes(30)
                .specialty(Specialty.GENERAL)
                .status(AppointmentStatus.PENDING)
                .notes("Revisión general")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private CreateAppointmentRequestDTO validCreateRequest() {
        CreateAppointmentRequestDTO dto = new CreateAppointmentRequestDTO();
        dto.setPatientId(PATIENT_ID);
        dto.setDoctorId(DOCTOR_ID);
        dto.setScheduledAt(LocalDateTime.now().plusDays(1));
        dto.setDurationMinutes(30);
        dto.setSpecialty(Specialty.GENERAL);
        dto.setNotes("Revisión general");
        return dto;
    }

    // ── POST /api/appointments ────────────────────────────────────────────────

    @Test
    void create_returnsCreated() throws Exception {
        when(appointmentService.createAppointment(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/appointments")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId").value(PATIENT_ID.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void create_missingPatientId_returnsBadRequest() throws Exception {
        CreateAppointmentRequestDTO invalid = new CreateAppointmentRequestDTO();
        invalid.setDoctorId(DOCTOR_ID);
        invalid.setScheduledAt(LocalDateTime.now().plusDays(1));
        invalid.setSpecialty(Specialty.GENERAL);
        // patientId null → @NotNull falla

        mockMvc.perform(post("/api/appointments")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void create_scheduleConflict_returnsConflict() throws Exception {
        when(appointmentService.createAppointment(any()))
                .thenThrow(new AppointmentConflictException("El doctor ya tiene una cita en ese horario"));

        mockMvc.perform(post("/api/appointments")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void create_withoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/appointments ─────────────────────────────────────────────────

    @Test
    void getAll_returnsOkWithList() throws Exception {
        when(appointmentService.getAllAppointments())
                .thenReturn(List.of(sampleResponse(), sampleResponse()));

        mockMvc.perform(get("/api/appointments").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAll_emptyList_returnsOk() throws Exception {
        when(appointmentService.getAllAppointments()).thenReturn(List.of());

        mockMvc.perform(get("/api/appointments").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── GET /api/appointments/{id} ────────────────────────────────────────────

    @Test
    void getById_found_returnsOk() throws Exception {
        AppointmentResponseDTO response = sampleResponse();
        when(appointmentService.getAppointmentById(response.getId())).thenReturn(response);

        mockMvc.perform(get("/api/appointments/{id}", response.getId()).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialty").value("GENERAL"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(appointmentService.getAppointmentById(id))
                .thenThrow(new AppointmentNotFoundException(id));

        mockMvc.perform(get("/api/appointments/{id}", id).with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    // ── GET /api/appointments/patient/{patientId} ─────────────────────────────

    @Test
    void getByPatient_returnsOkWithList() throws Exception {
        when(appointmentService.getByPatient(PATIENT_ID))
                .thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/appointments/patient/{id}", PATIENT_ID).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].patientId").value(PATIENT_ID.toString()));
    }

    // ── GET /api/appointments/doctor/{doctorId} ───────────────────────────────

    @Test
    void getByDoctor_returnsOkWithList() throws Exception {
        when(appointmentService.getByDoctor(DOCTOR_ID))
                .thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/appointments/doctor/{id}", DOCTOR_ID).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].doctorId").value(DOCTOR_ID.toString()));
    }

    // ── PUT /api/appointments/{id} ────────────────────────────────────────────

    @Test
    void update_returnsOk() throws Exception {
        AppointmentResponseDTO response = sampleResponse();
        response.setStatus(AppointmentStatus.CONFIRMED);
        when(appointmentService.updateAppointment(eq(response.getId()), any()))
                .thenReturn(response);

        UpdateAppointmentRequestDTO dto = new UpdateAppointmentRequestDTO();
        dto.setStatus(AppointmentStatus.CONFIRMED);

        mockMvc.perform(put("/api/appointments/{id}", response.getId())
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void update_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(appointmentService.updateAppointment(eq(id), any()))
                .thenThrow(new AppointmentNotFoundException(id));

        UpdateAppointmentRequestDTO dto = new UpdateAppointmentRequestDTO();
        dto.setStatus(AppointmentStatus.CONFIRMED);

        mockMvc.perform(put("/api/appointments/{id}", id)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    // ── PUT /api/appointments/{id}/cancel ─────────────────────────────────────

    @Test
    void cancel_returnsOk() throws Exception {
        AppointmentResponseDTO response = sampleResponse();
        response.setStatus(AppointmentStatus.CANCELLED);
        when(appointmentService.cancelAppointment(response.getId())).thenReturn(response);

        mockMvc.perform(put("/api/appointments/{id}/cancel", response.getId()).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancel_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(appointmentService.cancelAppointment(id))
                .thenThrow(new AppointmentNotFoundException(id));

        mockMvc.perform(put("/api/appointments/{id}/cancel", id).with(jwt()))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/appointments/{id} ─────────────────────────────────────────

    @Test
    void delete_returnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(appointmentService).deleteAppointment(id);

        mockMvc.perform(delete("/api/appointments/{id}", id).with(jwt()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new AppointmentNotFoundException(id))
                .when(appointmentService).deleteAppointment(id);

        mockMvc.perform(delete("/api/appointments/{id}", id).with(jwt()))
                .andExpect(status().isNotFound());
    }
}
