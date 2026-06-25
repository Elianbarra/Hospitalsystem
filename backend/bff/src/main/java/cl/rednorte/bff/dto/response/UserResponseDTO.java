package cl.rednorte.bff.dto.response;

public record UserResponseDTO(
        String id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String documentType,
        String documentNumber,
        String role,
        String specialty,
        boolean isActive,
        String createdAt
) {}
