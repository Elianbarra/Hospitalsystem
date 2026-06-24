package cl.rednorte.bff.dto.response;

public record TokenValidationResponseDTO(
        boolean valid,
        String userId,
        String email,
        String role
) {}
