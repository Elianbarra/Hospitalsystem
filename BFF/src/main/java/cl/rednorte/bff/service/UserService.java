package cl.rednorte.bff.service;

import cl.rednorte.bff.exception.ApiException;
import cl.rednorte.bff.model.request.CreateUserRequest;
import cl.rednorte.bff.model.request.UpdateUserRequest;
import cl.rednorte.bff.model.response.UserResponse;
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

    public UserResponse register(CreateUserRequest request) {
        log.debug("BFF → ms-user  POST /api/users/register  role={}", request.role());
        try {
            return userClient.post()
                    .uri("/api/users/register")
                    .body(request)
                    .retrieve()
                    .body(UserResponse.class);
        } catch (HttpClientErrorException e) {
            throw new ApiException(e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException(e.getStatusCode().value(), "ms-user error");
        } catch (ResourceAccessException e) {
            throw new ApiException(503, "ms-user unavailable");
        }
    }

    public List<UserResponse> getAll() {
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

    public UserResponse getById(String id) {
        log.debug("BFF → ms-user  GET /api/users/{}", id);
        try {
            return userClient.get()
                    .uri("/api/users/{id}", id)
                    .retrieve()
                    .body(UserResponse.class);
        } catch (HttpClientErrorException e) {
            throw new ApiException(e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException(e.getStatusCode().value(), "ms-user error");
        } catch (ResourceAccessException e) {
            throw new ApiException(503, "ms-user unavailable");
        }
    }

    public UserResponse update(String id, UpdateUserRequest request) {
        log.debug("BFF → ms-user  PUT /api/users/{}", id);
        try {
            return userClient.put()
                    .uri("/api/users/{id}", id)
                    .body(request)
                    .retrieve()
                    .body(UserResponse.class);
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
