package cl.rednorte.bff.service;

import cl.rednorte.bff.exception.ApiException;
import cl.rednorte.bff.dto.request.CreateAppointmentRequestDTO;
import cl.rednorte.bff.dto.request.UpdateAppointmentRequestDTO;
import cl.rednorte.bff.dto.response.AppointmentResponseDTO;
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
 * Orquesta las llamadas al microservicio ms-appointment.
 * <p>
 * TODO: ms-appointment aún no está implementado.
 *       Conectar cuando el microservicio esté disponible en MS_APPOINTMENTS_URL.
 *       Los endpoints del ms-appointment deben seguir el contrato definido en los modelos.
 * </p>
 */
@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private final RestClient appointmentClient;

    public AppointmentService(@Qualifier("appointmentClient") RestClient appointmentClient) {
        this.appointmentClient = appointmentClient;
    }

    public AppointmentResponseDTO create(CreateAppointmentRequestDTO request) {
        log.debug("BFF → ms-appointment  POST /api/appointments  patient={}", request.patientId());
        try {
            return appointmentClient.post()
                    .uri("/api/appointments")
                    .body(request)
                    .retrieve()
                    .body(AppointmentResponseDTO.class);
        } catch (HttpClientErrorException e) {
            throw new ApiException(e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException(e.getStatusCode().value(), "ms-appointment error");
        } catch (ResourceAccessException e) {
            throw new ApiException(503, "ms-appointment unavailable — pendiente de implementación");
        }
    }

    public List<AppointmentResponseDTO> getAll() {
        log.debug("BFF → ms-appointment  GET /api/appointments");
        try {
            return appointmentClient.get()
                    .uri("/api/appointments")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (HttpClientErrorException e) {
            throw new ApiException(e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException(e.getStatusCode().value(), "ms-appointment error");
        } catch (ResourceAccessException e) {
            throw new ApiException(503, "ms-appointment unavailable — pendiente de implementación");
        }
    }

    public List<AppointmentResponseDTO> getByPatient(String patientId) {
        log.debug("BFF → ms-appointment  GET /api/appointments/patient/{}", patientId);
        try {
            return appointmentClient.get()
                    .uri("/api/appointments/patient/{patientId}", patientId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (HttpClientErrorException e) {
            throw new ApiException(e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException(e.getStatusCode().value(), "ms-appointment error");
        } catch (ResourceAccessException e) {
            throw new ApiException(503, "ms-appointment unavailable — pendiente de implementación");
        }
    }

    public List<AppointmentResponseDTO> getByDoctor(String doctorId) {
        log.debug("BFF → ms-appointment  GET /api/appointments/doctor/{}", doctorId);
        try {
            return appointmentClient.get()
                    .uri("/api/appointments/doctor/{doctorId}", doctorId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (HttpClientErrorException e) {
            throw new ApiException(e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException(e.getStatusCode().value(), "ms-appointment error");
        } catch (ResourceAccessException e) {
            throw new ApiException(503, "ms-appointment unavailable — pendiente de implementación");
        }
    }

    public AppointmentResponseDTO getById(String id) {
        log.debug("BFF → ms-appointment  GET /api/appointments/{}", id);
        try {
            return appointmentClient.get()
                    .uri("/api/appointments/{id}", id)
                    .retrieve()
                    .body(AppointmentResponseDTO.class);
        } catch (HttpClientErrorException e) {
            throw new ApiException(e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException(e.getStatusCode().value(), "ms-appointment error");
        } catch (ResourceAccessException e) {
            throw new ApiException(503, "ms-appointment unavailable — pendiente de implementación");
        }
    }

    public AppointmentResponseDTO update(String id, UpdateAppointmentRequestDTO request) {
        log.debug("BFF → ms-appointment  PUT /api/appointments/{}", id);
        try {
            return appointmentClient.put()
                    .uri("/api/appointments/{id}", id)
                    .body(request)
                    .retrieve()
                    .body(AppointmentResponseDTO.class);
        } catch (HttpClientErrorException e) {
            throw new ApiException(e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException(e.getStatusCode().value(), "ms-appointment error");
        } catch (ResourceAccessException e) {
            throw new ApiException(503, "ms-appointment unavailable — pendiente de implementación");
        }
    }

    public void cancel(String id) {
        log.debug("BFF → ms-appointment  DELETE /api/appointments/{}", id);
        try {
            appointmentClient.delete()
                    .uri("/api/appointments/{id}", id)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException e) {
            throw new ApiException(e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException(e.getStatusCode().value(), "ms-appointment error");
        } catch (ResourceAccessException e) {
            throw new ApiException(503, "ms-appointment unavailable — pendiente de implementación");
        }
    }

    /**
     * Cancela la cita registrando que fue el médico quien canceló.
     * El BFF llama a PUT /api/appointments/{id}/cancel-doctor en ms-appointment.
     * La reasignación automática la orquesta ReasignacionService.
     */
    public AppointmentResponseDTO cancelByDoctor(String id) {
        log.debug("BFF → ms-appointment  PUT /api/appointments/{}/cancel-doctor", id);
        try {
            return appointmentClient.put()
                    .uri("/api/appointments/{id}/cancel-doctor", id)
                    .retrieve()
                    .body(AppointmentResponseDTO.class);
        } catch (HttpClientErrorException e) {
            throw new ApiException(e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException(e.getStatusCode().value(), "ms-appointment error");
        } catch (ResourceAccessException e) {
            throw new ApiException(503, "ms-appointment unavailable");
        }
    }

    /**
     * Cancela la cita registrando que fue el paciente quien canceló.
     * El BFF llama a PUT /api/appointments/{id}/cancel-patient en ms-appointment.
     * El reencola en waitlist lo orquesta ReasignacionService.
     */
    public AppointmentResponseDTO cancelByPatient(String id) {
        log.debug("BFF → ms-appointment  PUT /api/appointments/{}/cancel-patient", id);
        try {
            return appointmentClient.put()
                    .uri("/api/appointments/{id}/cancel-patient", id)
                    .retrieve()
                    .body(AppointmentResponseDTO.class);
        } catch (HttpClientErrorException e) {
            throw new ApiException(e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException(e.getStatusCode().value(), "ms-appointment error");
        } catch (ResourceAccessException e) {
            throw new ApiException(503, "ms-appointment unavailable");
        }
    }
}
