package cl.rednorte.bff.service;

import cl.rednorte.bff.exception.ApiException;
import cl.rednorte.bff.dto.request.CreateUserRequestDTO;
import cl.rednorte.bff.dto.request.UpdateUserRequestDTO;
import cl.rednorte.bff.dto.response.UserResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Orquesta las llamadas al microservicio ms-user.
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final RestClient userClient;

    public UserService(@Qualifier("userClient") RestClient userClient) {
        this.userClient = userClient;
    }

    public UserResponseDTO register(CreateUserRequestDTO request) {
        log.debug("BFF → ms-user  POST /api/users/register  role={}", request.role());
        try {
            return userClient.post()
                    .uri("/api/users/register")
                    .body(request)
                    .retrieve()
                    .body(UserResponseDTO.class);
        } catch (HttpClientErrorException e) {
            throw new ApiException(e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException(e.getStatusCode().value(), "ms-user error");
        } catch (ResourceAccessException e) {
            throw new ApiException(503, "ms-user unavailable");
        }
    }

    public List<UserResponseDTO> getAll() {
        log.debug("BFF → ms-user  GET /api/users");
        try {
            return userClient.get()
                    .uri("/api/users")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (HttpClientErrorException e) {
            throw new ApiException(e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException(e.getStatusCode().value(), "ms-user error");
        } catch (ResourceAccessException e) {
            throw new ApiException(503, "ms-user unavailable");
        }
    }

    public UserResponseDTO getById(String id) {
        log.debug("BFF → ms-user  GET /api/users/{}", id);
        try {
            return userClient.get()
                    .uri("/api/users/{id}", id)
                    .retrieve()
                    .body(UserResponseDTO.class);
        } catch (HttpClientErrorException e) {
            throw new ApiException(e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException(e.getStatusCode().value(), "ms-user error");
        } catch (ResourceAccessException e) {
            throw new ApiException(503, "ms-user unavailable");
        }
    }

    public UserResponseDTO update(String id, UpdateUserRequestDTO request) {
        log.debug("BFF → ms-user  PUT /api/users/{}", id);
        try {
            return userClient.put()
                    .uri("/api/users/{id}", id)
                    .body(request)
                    .retrieve()
                    .body(UserResponseDTO.class);
        } catch (HttpClientErrorException e) {
            throw new ApiException(e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException(e.getStatusCode().value(), "ms-user error");
        } catch (ResourceAccessException e) {
            throw new ApiException(503, "ms-user unavailable");
        }
    }

    public List<UserResponseDTO> getBySpecialty(String specialty) {
        log.debug("BFF → ms-user  GET /api/users/specialty/{}", specialty);
        try {
            return userClient.get()
                    .uri("/api/users/specialty/{specialty}", specialty)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (HttpClientErrorException e) {
            throw new ApiException(e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException(e.getStatusCode().value(), "ms-user error");
        } catch (ResourceAccessException e) {
            throw new ApiException(503, "ms-user unavailable");
        }
    }

    public void deactivate(String id) {
        log.debug("BFF → ms-user  DELETE /api/users/{}", id);
        try {
            userClient.delete()
                    .uri("/api/users/{id}", id)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException e) {
            throw new ApiException(e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException(e.getStatusCode().value(), "ms-user error");
        } catch (ResourceAccessException e) {
            throw new ApiException(503, "ms-user unavailable");
        }
    }
}
