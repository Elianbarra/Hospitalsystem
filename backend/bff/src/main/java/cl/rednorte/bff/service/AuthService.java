package cl.rednorte.bff.service;

import cl.rednorte.bff.exception.ApiException;
import cl.rednorte.bff.dto.request.LoginRequestDTO;
import cl.rednorte.bff.dto.response.AuthResponseDTO;
import cl.rednorte.bff.dto.response.TokenValidationResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * Orquesta las llamadas al microservicio ms-auth.
 * No contiene lógica de negocio propia: delega todo al microservicio.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final RestClient authClient;

    public AuthService(@Qualifier("authClient") RestClient authClient) {
        this.authClient = authClient;
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        log.debug("BFF → ms-auth  POST /api/auth/login  email={}", request.email());
        try {
            return authClient.post()
                    .uri("/api/auth/login")
                    .body(request)
                    .retrieve()
                    .body(AuthResponseDTO.class);
        } catch (HttpClientErrorException e) {
            throw new ApiException(e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException(e.getStatusCode().value(), "ms-auth error");
        } catch (ResourceAccessException e) {
            throw new ApiException(503, "ms-auth unavailable");
        }
    }

    public TokenValidationResponseDTO validateToken(String token) {
        log.debug("BFF → ms-auth  GET /api/auth/validate");
        try {
            return authClient.get()
                    .uri(uri -> uri.path("/api/auth/validate").queryParam("token", token).build())
                    .retrieve()
                    .body(TokenValidationResponseDTO.class);
        } catch (HttpClientErrorException e) {
            throw new ApiException(e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException(e.getStatusCode().value(), "ms-auth error");
        } catch (ResourceAccessException e) {
            throw new ApiException(503, "ms-auth unavailable");
        }
    }
}
