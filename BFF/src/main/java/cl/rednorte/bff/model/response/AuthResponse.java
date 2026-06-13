package cl.rednorte.bff.model.response;

public record AuthResponse(
        String token,
        String tokenType,
        String userId,
        String email,
        String role
) {}
