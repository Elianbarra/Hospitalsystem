package cl.rednorte.bff.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String phone,
        @NotBlank String documentType,
        @NotBlank String documentNumber,
        @NotBlank String role,
        String specialty   // obligatorio cuando role = DOCTOR
) {}
