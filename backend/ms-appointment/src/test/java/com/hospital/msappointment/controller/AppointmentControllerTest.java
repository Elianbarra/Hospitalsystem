package com.hospital.msappointment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.msappointment.dto.request.CreateAppointmentRequestDTO;
import com.hospital.msappointment.dto.response.AppointmentResponseDTO;
import com.hospital.msappointment.entity.enums.AppointmentStatus;
import com.hospital.msappointment.entity.enums.AppointmentType;
import com.hospital.msappointment.entity.enums.CancelledBy;
import com.hospital.msappointment.entity.enums.Specialty;
import com.hospital.msappointment.exception.AppointmentNotFoundException;
import com.hospital.msappointment.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de controlador para AppointmentController.
 *
 * Usa @SpringBootTest + @AutoConfigureMockMvc en lugar de @WebMvcTest porque
 * SecurityConfig tiene @EnableWebSecurity y se carga siempre en el contexto web.
 * El JwtDecoder se mockea para que devuelva un JWT válido sin conectar a ms-auth.
 * Los valores de datasource y flyway los provee src/test/resources/application.properties.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

    /**
     * Mockea el JwtDecoder para que Spring Security acepte el token "fake-token"
     * sin intentar validarlo contra el JWKS endpoint de ms-auth.
     */
    @MockBean
    private JwtDecoder jwtDecoder;

    /** Cabecera de autorización usada en todas las peticiones autenticadas */
    private static final String AUTH = "Bearer fake-token";

    @BeforeEach
    void setupJwt() {
        // Devuelve un JWT con el claim "role" que usa JwtAuthenticationConverter en SecurityConfig
        Jwt jwt = Jwt.withTokenValue("fake-token")
                .header("alg", "RS256")
                .claim("sub", "test-doctor")
                .claim("role", "DOCTOR")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private static final UUID PATIENT_ID = UUID.randomUUID();
    private static final UUID DOCTOR_ID  = UUID.randomUUID();

    private AppointmentResponseDTO sampleResponse() {
        return AppointmentResponseDTO.builder()
                .id(UUID.randomUUID())
                .patientId(PATIENT_ID)
                .doctorId(DOCTOR_ID)
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .durationMinutes(30)
                .specialty(Specialty.CARDIOLOGY)
                .appointmentType(AppointmentType.CONSULTA)
                .status(AppointmentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
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

    // ─── POST /api/appointments ───────────────────────────────────────────────

    @Test
    void create_returnsCreated() throws Exception {
        when(appointmentService.createAppointment(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCreateRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.specialty").value("CARDIOLOGY"))
                .andExpect(jsonPath("$.appointmentType").value("CONSULTA"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void create_withInvalidBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateAppointmentRequestDTO())))
                .andExpect(status().isBadRequest());
    }

    // ─── GET /api/appointments ────────────────────────────────────────────────

    @Test
    void getAll_returnsOkWithList() throws Exception {
        when(appointmentService.getAllAppointments()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/appointments").header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ─── GET /api/appointments/{id} ───────────────────────────────────────────

    @Test
    void getById_found_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(appointmentService.getAppointmentById(id)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/appointments/{id}", id).header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(appointmentService.getAppointmentById(id))
                .thenThrow(new AppointmentNotFoundException(id));

        mockMvc.perform(get("/api/appointments/{id}", id).header("Authorization", AUTH))
                .andExpect(status().isNotFound());
    }

    // ─── GET /api/appointments/patient/{patientId} ────────────────────────────

    @Test
    void getByPatient_returnsOk() throws Exception {
        when(appointmentService.getByPatient(PATIENT_ID)).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/appointments/patient/{id}", PATIENT_ID)
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientId").value(PATIENT_ID.toString()));
    }

    // ─── PUT /api/appointments/{id}/cancel-doctor ─────────────────────────────

    @Test
    void cancelByDoctor_returnsCancelledWithDoctorTag() throws Exception {
        UUID id = UUID.randomUUID();
        AppointmentResponseDTO cancelled = AppointmentResponseDTO.builder()
                .id(id).patientId(PATIENT_ID).doctorId(DOCTOR_ID)
                .specialty(Specialty.CARDIOLOGY).appointmentType(AppointmentType.CONSULTA)
                .status(AppointmentStatus.CANCELLED).cancelledBy(CancelledBy.DOCTOR)
                .scheduledAt(LocalDateTime.now().plusDays(1)).durationMinutes(30)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(appointmentService.cancelByDoctor(id)).thenReturn(cancelled);

        mockMvc.perform(put("/api/appointments/{id}/cancel-doctor", id)
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancelledBy").value("DOCTOR"));
    }

    @Test
    void cancelByDoctor_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(appointmentService.cancelByDoctor(id))
                .thenThrow(new AppointmentNotFoundException(id));

        mockMvc.perform(put("/api/appointments/{id}/cancel-doctor", id)
                        .header("Authorization", AUTH))
                .andExpect(status().isNotFound());
    }

    // ─── PUT /api/appointments/{id}/cancel-patient ────────────────────────────

    @Test
    void cancelByPatient_returnsCancelledWithPatientTag() throws Exception {
        UUID id = UUID.randomUUID();
        AppointmentResponseDTO cancelled = AppointmentResponseDTO.builder()
                .id(id).patientId(PATIENT_ID).doctorId(DOCTOR_ID)
                .specialty(Specialty.CARDIOLOGY).appointmentType(AppointmentType.CONSULTA)
                .status(AppointmentStatus.CANCELLED).cancelledBy(CancelledBy.PATIENT)
                .scheduledAt(LocalDateTime.now().plusDays(1)).durationMinutes(30)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(appointmentService.cancelByPatient(id)).thenReturn(cancelled);

        mockMvc.perform(put("/api/appointments/{id}/cancel-patient", id)
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancelledBy").value("PATIENT"));
    }

    // ─── DELETE /api/appointments/{id} ────────────────────────────────────────

    @Test
    void delete_returnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(appointmentService).deleteAppointment(id);

        mockMvc.perform(delete("/api/appointments/{id}", id).header("Authorization", AUTH))
                .andExpect(status().isNoContent());
    }
}
