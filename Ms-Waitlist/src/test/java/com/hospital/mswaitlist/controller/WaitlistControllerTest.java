package com.hospital.mswaitlist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hospital.mswaitlist.dto.request.CreateWaitlistEntryRequestDTO;
import com.hospital.mswaitlist.dto.request.UpdateWaitlistEntryRequestDTO;
import com.hospital.mswaitlist.dto.response.WaitlistEntryResponseDTO;
import com.hospital.mswaitlist.entity.enums.Priority;
import com.hospital.mswaitlist.entity.enums.Specialty;
import com.hospital.mswaitlist.entity.enums.WaitlistStatus;
import com.hospital.mswaitlist.exception.GlobalExceptionHandler;
import com.hospital.mswaitlist.exception.WaitlistEntryNotFoundException;
import com.hospital.mswaitlist.service.WaitlistService;
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

@WebMvcTest(WaitlistController.class)
@Import(GlobalExceptionHandler.class)
class WaitlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WaitlistService waitlistService;

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

    private WaitlistEntryResponseDTO sampleResponse() {
        return WaitlistEntryResponseDTO.builder()
                .id(UUID.randomUUID())
                .patientId(PATIENT_ID)
                .specialty(Specialty.MEDICINA_GENERAL)
                .priority(Priority.NORMAL)
                .status(WaitlistStatus.WAITING)
                .notes("Dolor de cabeza")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private CreateWaitlistEntryRequestDTO validCreateRequest() {
        return CreateWaitlistEntryRequestDTO.builder()
                .patientId(PATIENT_ID)
                .specialty(Specialty.MEDICINA_GENERAL)
                .priority(Priority.NORMAL)
                .notes("Dolor de cabeza")
                .build();
    }

    // ── POST /api/waitlist ────────────────────────────────────────────────────

    @Test
    void add_returnsCreated() throws Exception {
        when(waitlistService.addToWaitlist(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/waitlist")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId").value(PATIENT_ID.toString()))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.priority").value("NORMAL"));
    }

    @Test
    void add_missingPatientId_returnsBadRequest() throws Exception {
        CreateWaitlistEntryRequestDTO invalid = CreateWaitlistEntryRequestDTO.builder()
                .specialty(Specialty.MEDICINA_GENERAL)
                .build(); // patientId null → @NotNull falla

        mockMvc.perform(post("/api/waitlist")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void add_missingSpecialty_returnsBadRequest() throws Exception {
        CreateWaitlistEntryRequestDTO invalid = CreateWaitlistEntryRequestDTO.builder()
                .patientId(PATIENT_ID)
                .build(); // specialty null → @NotNull falla

        mockMvc.perform(post("/api/waitlist")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void add_withoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/waitlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/waitlist ─────────────────────────────────────────────────────

    @Test
    void getAll_returnsOkWithList() throws Exception {
        when(waitlistService.getAll())
                .thenReturn(List.of(sampleResponse(), sampleResponse()));

        mockMvc.perform(get("/api/waitlist").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAll_emptyList_returnsOk() throws Exception {
        when(waitlistService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/waitlist").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── GET /api/waitlist/{id} ────────────────────────────────────────────────

    @Test
    void getById_found_returnsOk() throws Exception {
        WaitlistEntryResponseDTO response = sampleResponse();
        when(waitlistService.getById(response.getId())).thenReturn(response);

        mockMvc.perform(get("/api/waitlist/{id}", response.getId()).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialty").value("MEDICINA_GENERAL"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(waitlistService.getById(id))
                .thenThrow(new WaitlistEntryNotFoundException(id));

        mockMvc.perform(get("/api/waitlist/{id}", id).with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    // ── GET /api/waitlist/patient/{patientId} ─────────────────────────────────

    @Test
    void getByPatient_returnsOkWithList() throws Exception {
        when(waitlistService.getByPatient(PATIENT_ID))
                .thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/waitlist/patient/{id}", PATIENT_ID).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].patientId").value(PATIENT_ID.toString()));
    }

    // ── GET /api/waitlist/specialty/{specialty} ───────────────────────────────

    @Test
    void getBySpecialty_returnsOkWithList() throws Exception {
        when(waitlistService.getBySpecialty(Specialty.MEDICINA_GENERAL))
                .thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/waitlist/specialty/{s}", Specialty.MEDICINA_GENERAL).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ── GET /api/waitlist/status/{status} ─────────────────────────────────────

    @Test
    void getByStatus_returnsOkWithList() throws Exception {
        when(waitlistService.getByStatus(WaitlistStatus.WAITING))
                .thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/waitlist/status/{s}", WaitlistStatus.WAITING).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ── PUT /api/waitlist/{id} ────────────────────────────────────────────────

    @Test
    void update_returnsOk() throws Exception {
        WaitlistEntryResponseDTO response = sampleResponse();
        response.setPriority(Priority.CRITICO);
        when(waitlistService.update(eq(response.getId()), any())).thenReturn(response);

        UpdateWaitlistEntryRequestDTO dto = UpdateWaitlistEntryRequestDTO.builder()
                .priority(Priority.CRITICO)
                .build();

        mockMvc.perform(put("/api/waitlist/{id}", response.getId())
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").value("CRITICO"));
    }

    @Test
    void update_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(waitlistService.update(eq(id), any()))
                .thenThrow(new WaitlistEntryNotFoundException(id));

        UpdateWaitlistEntryRequestDTO dto = UpdateWaitlistEntryRequestDTO.builder()
                .status(WaitlistStatus.NOTIFIED)
                .build();

        mockMvc.perform(put("/api/waitlist/{id}", id)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    // ── PUT /api/waitlist/{id}/cancel ─────────────────────────────────────────

    @Test
    void cancel_returnsOk() throws Exception {
        WaitlistEntryResponseDTO response = sampleResponse();
        response.setStatus(WaitlistStatus.CANCELLED);
        when(waitlistService.cancel(response.getId())).thenReturn(response);

        mockMvc.perform(put("/api/waitlist/{id}/cancel", response.getId()).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancel_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(waitlistService.cancel(id))
                .thenThrow(new WaitlistEntryNotFoundException(id));

        mockMvc.perform(put("/api/waitlist/{id}/cancel", id).with(jwt()))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/waitlist/{id} ─────────────────────────────────────────────

    @Test
    void delete_returnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(waitlistService).delete(id);

        mockMvc.perform(delete("/api/waitlist/{id}", id).with(jwt()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new WaitlistEntryNotFoundException(id))
                .when(waitlistService).delete(id);

        mockMvc.perform(delete("/api/waitlist/{id}", id).with(jwt()))
                .andExpect(status().isNotFound());
    }
}
