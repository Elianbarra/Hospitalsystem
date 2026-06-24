package cl.rednorte.bff.dto.response;

public record AuthResponseDTO(
        String token,
        String tokenType,
        String userId,
        String email,
        String role
) {}
