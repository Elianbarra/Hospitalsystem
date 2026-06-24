package cl.rednorte.bff.controller;

import cl.rednorte.bff.dto.request.CreateUserRequestDTO;
import cl.rednorte.bff.dto.request.UpdateUserRequestDTO;
import cl.rednorte.bff.dto.response.UserResponseDTO;
import cl.rednorte.bff.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller MVC — capa Users.
 *
 * Rutas expuestas por el BFF:
 *   GET    /api/users            → ms-user GET  /api/users
 *   POST   /api/users            → ms-user POST /api/users/register
 *   GET    /api/users/{id}       → ms-user GET  /api/users/{id}
 *   PUT    /api/users/{id}       → ms-user PUT  /api/users/{id}
 *   DELETE /api/users/{id}       → ms-user DELETE /api/users/{id}  (soft-delete)
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuarios", description = "Registro y gestión de pacientes, médicos y administrativos")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Listar usuarios", description = "Devuelve todos los usuarios registrados en el sistema")
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @Operation(summary = "Registrar usuario", description = "Crea un nuevo usuario y sus credenciales en ms-auth")
    @PostMapping
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody CreateUserRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @Operation(summary = "Obtener usuario", description = "Devuelve el perfil de un usuario por su ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @Operation(summary = "Actualizar usuario", description = "Modifica los datos de un usuario existente")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> update(
            @PathVariable String id,
            @RequestBody UpdateUserRequestDTO request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @Operation(summary = "Médicos por especialidad", description = "Lista médicos activos de una especialidad específica (CARDIOLOGY, NEUROLOGY, etc.)")
    @GetMapping("/specialty/{specialty}")
    public ResponseEntity<List<UserResponseDTO>> getBySpecialty(@PathVariable String specialty) {
        return ResponseEntity.ok(userService.getBySpecialty(specialty));
    }

    @Operation(summary = "Desactivar usuario", description = "Realiza un soft-delete desactivando la cuenta del usuario")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable String id) {
        userService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
