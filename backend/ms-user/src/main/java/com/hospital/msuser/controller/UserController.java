package com.hospital.msuser.controller;

import com.hospital.msuser.dto.request.CreateUserRequestDTO;
import com.hospital.msuser.dto.request.UpdateUserRequestDTO;
import com.hospital.msuser.dto.response.UserResponseDTO;
import com.hospital.msuser.service.UserService;
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

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody CreateUserRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(dto));
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<UserResponseDTO>> getAll() {
        return ResponseEntity.ok(userService.getAllActiveUsers());
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequestDTO dto) {
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }
}
