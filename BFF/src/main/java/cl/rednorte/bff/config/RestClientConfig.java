package cl.rednorte.bff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class RestClientConfig {

    /**
     * Interceptor que propaga el header Authorization: Bearer <token>
     * del request entrante al request saliente hacia los ms-*.
     * Solo aplica a clientes que requieren autenticación.
     */
    private static org.springframework.http.client.ClientHttpRequestInterceptor bearerTokenPropagator() {
        return (request, body, execution) -> {
            var attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes servletAttrs) {
                String auth = servletAttrs.getRequest().getHeader("Authorization");
                if (auth != null && auth.startsWith("Bearer ")) {
                    request.getHeaders().set("Authorization", auth);
                }
            }
            return execution.execute(request, body);
        };
    }

    /**
     * authClient: solo para login/register — NO propaga Bearer (usuario no tiene token todavía).
     */
    @Bean("authClient")
    public RestClient authRestClient(@Value("${ms.auth.url}") String authUrl) {
        return RestClient.builder()
                .baseUrl(authUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /**
     * userClient, appointmentClient, waitlistClient: propagan el Bearer token del usuario.
     */
    @Bean("userClient")
    public RestClient userRestClient(@Value("${ms.user.url}") String userUrl) {
        return RestClient.builder()
                .baseUrl(userUrl)
                .defaultHeader("Content-Type", "application/json")
                .requestInterceptor(bearerTokenPropagator())
                .build();
    }

    @Bean("appointmentClient")
    public RestClient appointmentRestClient(@Value("${ms.appointments.url}") String appointmentsUrl) {
        return RestClient.builder()
                .baseUrl(appointmentsUrl)
                .defaultHeader("Content-Type", "application/json")
                .requestInterceptor(bearerTokenPropagator())
                .build();
    }

    @Bean("waitlistClient")
    public RestClient waitlistRestClient(@Value("${ms.waitlist.url}") String waitlistUrl) {
        return RestClient.builder()
                .baseUrl(waitlistUrl)
                .defaultHeader("Content-Type", "application/json")
                .requestInterceptor(bearerTokenPropagator())
                .build();
    }
}
