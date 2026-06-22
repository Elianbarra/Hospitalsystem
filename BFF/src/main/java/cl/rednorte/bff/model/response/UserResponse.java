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
        boolean isActive,
        String createdAt
) {}
