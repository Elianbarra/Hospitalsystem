package com.hospital.msuser.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.msuser.service.UserService;
import com.hospital.msuser.dto.request.CreateUserRequestDTO;
import com.hospital.msuser.dto.request.UpdateUserRequestDTO;
import com.hospital.msuser.dto.response.UserResponseDTO;
import com.hospital.msuser.entity.enums.DocumentType;
import com.hospital.msuser.entity.enums.UserRole;
import com.hospital.msuser.exception.GlobalExceptionHandler;
import com.hospital.msuser.exception.UserAlreadyExistsException;
import com.hospital.msuser.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = UserController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private UserResponseDTO sampleResponse() {
        return UserResponseDTO.builder()
                .id(UUID.randomUUID())
                .firstName("Juan")
                .lastName("Perez")
                .email("juan@hospital.com")
                .phone("999888777")
                .documentType(DocumentType.DNI)
                .documentNumber("12345678")
                .role(UserRole.PATIENT)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private CreateUserRequestDTO sampleRequest() {
        return CreateUserRequestDTO.builder()
                .firstName("Juan")
                .lastName("Perez")
                .email("juan@hospital.com")
                .password("secreto123")
                .phone("999888777")
                .documentType(DocumentType.DNI)
                .documentNumber("12345678")
                .role(UserRole.PATIENT)
                .build();
    }

    @Test
    void register_returnsCreated() throws Exception {
        when(userService.registerUser(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("juan@hospital.com"))
                .andExpect(jsonPath("$.role").value("PATIENT"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void register_withInvalidBody_returnsBadRequest() throws Exception {
        CreateUserRequestDTO invalid = CreateUserRequestDTO.builder()
                .firstName("")
                .email("not-an-email")
                .build();

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void register_duplicateEmail_returnsConflict() throws Exception {
        when(userService.registerUser(any()))
                .thenThrow(new UserAlreadyExistsException("El email ya esta registrado: juan@hospital.com"));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("El email ya esta registrado: juan@hospital.com"));
    }

    @Test
    void getAll_returnsOkWithList() throws Exception {
        when(userService.getAllActiveUsers()).thenReturn(List.of(sampleResponse(), sampleResponse()));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].email").value("juan@hospital.com"));
    }

    @Test
    void getAll_emptyList_returnsOk() throws Exception {
        when(userService.getAllActiveUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getById_found_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        UserResponseDTO response = sampleResponse();
        when(userService.getUserById(id)).thenReturn(response);

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Juan"))
                .andExpect(jsonPath("$.lastName").value("Perez"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.getUserById(id))
                .thenThrow(new UserNotFoundException("Usuario no encontrado con id: " + id));

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void update_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateUserRequestDTO dto = UpdateUserRequestDTO.builder()
                .firstName("Carlos")
                .build();
        UserResponseDTO updated = sampleResponse();
        updated.setFirstName("Carlos");
        when(userService.updateUser(eq(id), any())).thenReturn(updated);

        mockMvc.perform(put("/api/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Carlos"));
    }

    @Test
    void update_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.updateUser(eq(id), any()))
                .thenThrow(new UserNotFoundException("Usuario no encontrado con id: " + id));

        mockMvc.perform(put("/api/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateUserRequestDTO.builder().firstName("X").build())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deactivate_returnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(userService).deactivateUser(id);

        mockMvc.perform(delete("/api/users/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void deactivate_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new UserNotFoundException("Usuario no encontrado con id: " + id))
                .when(userService).deactivateUser(id);

        mockMvc.perform(delete("/api/users/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }
}
