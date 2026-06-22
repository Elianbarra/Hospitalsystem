package cl.rednorte.bff.controller;

import cl.rednorte.bff.model.request.LoginRequest;
import cl.rednorte.bff.model.response.AuthResponse;
import cl.rednorte.bff.model.response.TokenValidationResponse;
import cl.rednorte.bff.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controller MVC — capa Auth.
 *
 * Rutas expuestas por el BFF:
 *   POST /api/auth/login          → ms-auth POST /api/auth/login
 *   GET  /api/auth/validate       → ms-auth GET  /api/auth/validate?token=
 */
@RestController
@RequestMapping("/api/auth")
@Validated
@Tag(name = "Autenticación", description = "Inicio de sesión y validación de tokens JWT")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Iniciar sesión", description = "Autentica al usuario y devuelve un token JWT")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Validar token", description = "Verifica si un token JWT es válido y no ha expirado")
    @GetMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validate(
            @RequestParam @NotBlank String token) {
        return ResponseEntity.ok(authService.validateToken(token));
    }
}
