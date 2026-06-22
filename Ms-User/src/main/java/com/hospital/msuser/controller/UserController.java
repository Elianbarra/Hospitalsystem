package com.hospital.msuser.controller;

import com.hospital.msuser.dto.request.CreateUserRequestDTO;
import com.hospital.msuser.dto.request.UpdateUserRequestDTO;
import com.hospital.msuser.dto.response.UserResponseDTO;
import com.hospital.msuser.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Gestión de usuarios (pacientes y doctores)")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Registrar usuario", description = "Crea un nuevo usuario y sus credenciales en ms-auth")
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody CreateUserRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(dto));
    }

    @Operation(summary = "Listar usuarios", description = "Devuelve todos los usuarios activos del sistema")
    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<UserResponseDTO>> getAll() {
        return ResponseEntity.ok(userService.getAllActiveUsers());
    }

    @Operation(summary = "Obtener usuario", description = "Devuelve el perfil de un usuario por su ID")
    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(summary = "Actualizar usuario", description = "Modifica los datos de un usuario existente")
    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequestDTO dto) {
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    @Operation(summary = "Desactivar usuario", description = "Realiza un soft-delete desactivando la cuenta del usuario")
    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }
}
