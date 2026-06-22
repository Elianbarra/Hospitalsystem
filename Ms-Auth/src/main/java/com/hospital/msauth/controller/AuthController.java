package com.hospital.msauth.controller;

import com.hospital.msauth.dto.request.LoginRequestDTO;
import com.hospital.msauth.dto.request.RegisterCredentialRequestDTO;
import com.hospital.msauth.dto.response.AuthResponseDTO;
import com.hospital.msauth.dto.response.TokenValidationResponseDTO;
import com.hospital.msauth.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Autenticación y emisión de tokens JWT")
public class AuthController {

    private final AuthService authService;

    // Llamado internamente por MS-USER al registrar un usuario
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterCredentialRequestDTO dto) {
        authService.registerCredential(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // Llamado por el cliente (frontend/app)
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    // Llamado por otros microservicios para validar el token
    @GetMapping("/validate")
    public ResponseEntity<TokenValidationResponseDTO> validate(@RequestParam String token) {
        return ResponseEntity.ok(authService.validateToken(token));
    }
}
