package cl.rednorte.bff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Configura los RestClient del BFF para cada microservicio.
 *
 * - authClient: NO lleva Bearer (es para login, el usuario aún no tiene token)
 * - userClient / appointmentClient / waitlistClient: propagan el Bearer del request
 *   entrante para que cada ms-* pueda validarlo independientemente (defensa en profundidad).
 */
@Configuration
public class RestClientConfig {

    /**
     * Interceptor que lee el header Authorization del request HTTP actual y lo
     * reenvía en cada llamada saliente de RestClient.
     * Si no hay request activo (ej. test unitario sin contexto web) no agrega nada.
     */
    @Bean
    public ClientHttpRequestInterceptor bearerTokenPropagator() {
        return (request, body, execution) -> {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                String authHeader = attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    request.getHeaders().set(HttpHeaders.AUTHORIZATION, authHeader);
                }
            }
            return execution.execute(request, body);
        };
    }

    // ── auth: login / registro — NO propaga Bearer ───────────────────────────
    @Bean("authClient")
    public RestClient authRestClient(@Value("${ms.auth.url}") String authUrl) {
        return RestClient.builder()
                .baseUrl(authUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    // ── ms-user — propaga Bearer ──────────────────────────────────────────────
    @Bean("userClient")
    public RestClient userRestClient(
            @Value("${ms.user.url}") String userUrl,
            ClientHttpRequestInterceptor bearerTokenPropagator) {
        return RestClient.builder()
                .baseUrl(userUrl)
                .defaultHeader("Content-Type", "application/json")
                .requestInterceptor(bearerTokenPropagator)
                .build();
    }

    // ── ms-appointment — propaga Bearer ──────────────────────────────────────
    @Bean("appointmentClient")
    public RestClient appointmentRestClient(
            @Value("${ms.appointments.url}") String appointmentsUrl,
            ClientHttpRequestInterceptor bearerTokenPropagator) {
        return RestClient.builder()
                .baseUrl(appointmentsUrl)
                .defaultHeader("Content-Type", "application/json")
                .requestInterceptor(bearerTokenPropagator)
                .build();
    }

    // ── ms-waitlist — propaga Bearer ──────────────────────────────────────────
    @Bean("waitlistClient")
    public RestClient waitlistRestClient(
            @Value("${ms.waitlist.url}") String waitlistUrl,
            ClientHttpRequestInterceptor bearerTokenPropagator) {
        return RestClient.builder()
                .baseUrl(waitlistUrl)
                .defaultHeader("Content-Type", "application/json")
                .requestInterceptor(bearerTokenPropagator)
                .build();
    }
}
