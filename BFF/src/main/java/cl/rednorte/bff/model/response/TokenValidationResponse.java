package cl.rednorte.bff.model.response;

public record TokenValidationResponse(
        boolean valid,
        String userId,
        String email,
        String role
) {}
