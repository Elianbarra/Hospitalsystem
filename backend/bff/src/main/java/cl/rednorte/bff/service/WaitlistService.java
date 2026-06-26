package cl.rednorte.bff.service;

import cl.rednorte.bff.exception.ApiException;
import cl.rednorte.bff.dto.request.CreateWaitlistEntryRequestDTO;
import cl.rednorte.bff.dto.request.UpdateWaitlistEntryRequestDTO;
import cl.rednorte.bff.dto.response.WaitlistEntryResponseDTO;
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


@Service
public class WaitlistService {

    private static final Logger log = LoggerFactory.getLogger(WaitlistService.class);

    private final RestClient waitlistClient;

    public WaitlistService(@Qualifier("waitlistClient") RestClient waitlistClient) {
        this.waitlistClient = waitlistClient;
    }

    public WaitlistEntryResponseDTO enqueue(CreateWaitlistEntryRequestDTO request) {
        log.debug("BFF → ms-waitlist  POST /api/waitlist  patient={}", request.patientId());
        try {
            return waitlistClient.post()
                    .uri("/api/waitlist")
                    .body(request)
                    .retrieve()
                    .body(WaitlistEntryResponseDTO.class);
        } catch (HttpClientErrorException e) {
            throw new ApiException(e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException(e.getStatusCode().value(), "ms-waitlist error");
        } catch (ResourceAccessException e) {
            throw new ApiException(503, "ms-waitlist unavailable — pendiente de implementación");
        }
    }

    public List<WaitlistEntryResponseDTO> getAll() {
        log.debug("BFF → ms-waitlist  GET /api/waitlist");
        try {
            return waitlistClient.get()
                    .uri("/api/waitlist")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (HttpClientErrorException e) {
            throw new ApiException(e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException(e.getStatusCode().value(), "ms-waitlist error");
        } catch (ResourceAccessException e) {
            throw new ApiException(503, "ms-waitlist unavailable — pendiente de implementación");
        }
    }

    public List<WaitlistEntryResponseDTO> getByPatient(String patientId) {
        log.debug("BFF → ms-waitlist  GET /api/waitlist/patient/{}", patientId);
        try {
            return waitlistClient.get()
                    .uri("/api/waitlist/patient/{patientId}", patientId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (HttpClientErrorException e) {
            throw new ApiException(e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException(e.getStatusCode().value(), "ms-waitlist error");
        } catch (ResourceAccessException e) {
            throw new ApiException(503, "ms-waitlist unavailable — pendiente de implementación");
        }
    }

    public WaitlistEntryResponseDTO getById(String id) {
        log.debug("BFF → ms-waitlist  GET /api/waitlist/{}", id);
        try {
            return waitlistClient.get()
                    .uri("/api/waitlist/{id}", id)
                    .retrieve()
                    .body(WaitlistEntryResponseDTO.class);
        } catch (HttpClientErrorException e) {
            throw new ApiException(e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException(e.getStatusCode().value(), "ms-waitlist error");
        } catch (ResourceAccessException e) {
            throw new ApiException(503, "ms-waitlist unavailable — pendiente de implementación");
        }
    }

    public List<WaitlistEntryResponseDTO> getBySpecialty(String specialty) {
        log.debug("BFF → ms-waitlist  GET /api/waitlist/specialty/{}", specialty);
        try {
            return waitlistClient.get()
                    .uri("/api/waitlist/specialty/{specialty}", specialty)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (HttpClientErrorException e) {
            throw new ApiException(e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException(e.getStatusCode().value(), "ms-waitlist error");
        } catch (ResourceAccessException e) {
            throw new ApiException(503, "ms-waitlist unavailable");
        }
    }

    public WaitlistEntryResponseDTO cancel(String id) {
        log.debug("BFF → ms-waitlist  PUT /api/waitlist/{}/cancel", id);
        try {
            return waitlistClient.put()
                    .uri("/api/waitlist/{id}/cancel", id)
                    .retrieve()
                    .body(WaitlistEntryResponseDTO.class);
        } catch (HttpClientErrorException e) {
            throw new ApiException(e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException(e.getStatusCode().value(), "ms-waitlist error");
        } catch (ResourceAccessException e) {
            throw new ApiException(503, "ms-waitlist unavailable");
        }
    }

    public WaitlistEntryResponseDTO update(String id, UpdateWaitlistEntryRequestDTO request) {
        log.debug("BFF → ms-waitlist  PUT /api/waitlist/{}  priority={}", id, request.priority());
        try {
            return waitlistClient.put()
                    .uri("/api/waitlist/{id}", id)
                    .body(request)
                    .retrieve()
                    .body(WaitlistEntryResponseDTO.class);
        } catch (HttpClientErrorException e) {
            throw new ApiException(e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException(e.getStatusCode().value(), "ms-waitlist error");
        } catch (ResourceAccessException e) {
            throw new ApiException(503, "ms-waitlist unavailable");
        }
    }

    public void remove(String id) {
        log.debug("BFF → ms-waitlist  DELETE /api/waitlist/{}", id);
        try {
            waitlistClient.delete()
                    .uri("/api/waitlist/{id}", id)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException e) {
            throw new ApiException(e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException(e.getStatusCode().value(), "ms-waitlist error");
        } catch (ResourceAccessException e) {
            throw new ApiException(503, "ms-waitlist unavailable — pendiente de implementación");
        }
    }

    /**
     * Devuelve el siguiente paciente en cola (WAITING) para una especialidad.
     * Ordenamiento aplicado en ms-waitlist: vitalRisk → priority → requeuedAt.
     * Retorna null si no hay nadie en espera.
     */
    public WaitlistEntryResponseDTO getNextForSpecialty(String specialty) {
        log.debug("BFF → ms-waitlist  GET /api/waitlist/specialty/{}/next", specialty);
        try {
            return waitlistClient.get()
                    .uri("/api/waitlist/specialty/{specialty}/next", specialty)
                    .retrieve()
                    .body(WaitlistEntryResponseDTO.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 204 || e.getStatusCode().value() == 404) return null;
            throw new ApiException(e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException(e.getStatusCode().value(), "ms-waitlist error");
        } catch (ResourceAccessException e) {
            throw new ApiException(503, "ms-waitlist unavailable");
        }
    }

    /**
     * Mueve al paciente al final de la cola (resetea requeuedAt a now).
     * Se invoca cuando el paciente cancela su cita.
     */
    public WaitlistEntryResponseDTO requeueToEnd(String waitlistEntryId) {
        log.debug("BFF → ms-waitlist  PUT /api/waitlist/{}/requeue", waitlistEntryId);
        try {
            return waitlistClient.put()
                    .uri("/api/waitlist/{id}/requeue", waitlistEntryId)
                    .retrieve()
                    .body(WaitlistEntryResponseDTO.class);
        } catch (HttpClientErrorException e) {
            throw new ApiException(e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException(e.getStatusCode().value(), "ms-waitlist error");
        } catch (ResourceAccessException e) {
            throw new ApiException(503, "ms-waitlist unavailable");
        }
    }

    /**
     * Marca una entrada como OFFERED (slot ofrecido al paciente, en espera de confirmación).
     */
    public WaitlistEntryResponseDTO markAsOffered(String waitlistEntryId) {
        log.debug("BFF → ms-waitlist  PUT /api/waitlist/{} status=OFFERED", waitlistEntryId);
        try {
            return waitlistClient.put()
                    .uri("/api/waitlist/{id}", waitlistEntryId)
                    .body(new cl.rednorte.bff.dto.request.UpdateWaitlistEntryRequestDTO("OFFERED", null, null))
                    .retrieve()
                    .body(WaitlistEntryResponseDTO.class);
        } catch (HttpClientErrorException e) {
            throw new ApiException(e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException(e.getStatusCode().value(), "ms-waitlist error");
        } catch (ResourceAccessException e) {
            throw new ApiException(503, "ms-waitlist unavailable");
        }
    }
}
