package cl.rednorte.bff.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Todos los campos son opcionales: solo se envían los que el cliente desea actualizar.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateUserRequestDTO(
        String firstName,
        String lastName,
        String phone,
        String documentType,
        String documentNumber,
        String role
) {}
