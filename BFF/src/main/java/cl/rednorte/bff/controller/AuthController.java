package cl.rednorte.bff.controller;

import cl.rednorte.bff.model.request.LoginRequest;
import cl.rednorte.bff.model.response.AuthResponse;
import cl.rednorte.bff.model.response.TokenValidationResponse;
import cl.rednorte.bff.service.AuthService;
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
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validate(
            @RequestParam @NotBlank String token) {
        return ResponseEntity.ok(authService.validateToken(token));
    }
}
