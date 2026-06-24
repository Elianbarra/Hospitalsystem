package cl.rednorte.bff.model.response;

public record UserResponse(
        String id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String documentType,
        String documentNumber,
        String role,
        String specialty,   // solo para DOCTOR; null para otros roles
        boolean isActive,
        String createdAt
) {}
