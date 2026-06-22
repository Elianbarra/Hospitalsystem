package cl.rednorte.bff.controller;

import cl.rednorte.bff.model.request.CreateUserRequest;
import cl.rednorte.bff.model.request.UpdateUserRequest;
import cl.rednorte.bff.model.response.UserResponse;
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
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @Operation(summary = "Registrar usuario", description = "Crea un nuevo usuario y sus credenciales en ms-auth")
    @PostMapping
    public ResponseEntity<UserResponse> register(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @Operation(summary = "Obtener usuario", description = "Devuelve el perfil de un usuario por su ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @Operation(summary = "Actualizar usuario", description = "Modifica los datos de un usuario existente")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable String id,
            @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @Operation(summary = "Desactivar usuario", description = "Realiza un soft-delete desactivando la cuenta del usuario")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable String id) {
        userService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
