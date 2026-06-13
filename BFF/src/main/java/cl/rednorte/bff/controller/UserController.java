package cl.rednorte.bff.controller;

import cl.rednorte.bff.model.request.CreateUserRequest;
import cl.rednorte.bff.model.request.UpdateUserRequest;
import cl.rednorte.bff.model.response.UserResponse;
import cl.rednorte.bff.service.UserService;
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
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @PostMapping
    public ResponseEntity<UserResponse> register(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable String id,
            @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable String id) {
        userService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
