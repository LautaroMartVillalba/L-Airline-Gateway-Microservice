package ar.com.l_airline.gateway_microservice.cookies;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Component responsible for interacting with the User microservice via HTTP requests
 * using a reactive {@link WebClient}. This class provides methods for token-related
 * operations such as creation and refresh.
 *
 */
@Component
public class UserMicroserviceQueries {

    /**
     * WebClient instance configured to communicate with the User microservice.
     */
    private final WebClient client  = WebClient.builder()
                                        .baseUrl("http://jwt-user:9000")
                                        .build();

    /**
     * Sends a POST request to the User microservice to generate a new JWT token
     * based on the provided email and password credentials.
     *
     * @param email    the user's email address
     * @param password the user's password
     * @return a {@link Mono} emitting the JWT as a {@link String}
     */
    public Mono<String> createToken(String email, String password){
        return client.post()
                .uri(uriBuilder -> uriBuilder.path("/auth/token")
                        .queryParam("email", email)
                        .queryParam("password", password)
                        .build())
                .retrieve()
                .bodyToMono(String.class);
    }

    /**
     * Sends a POST request to the User microservice to refresh an expired or invalid JWT token.
     * Requires both the token to be refreshed and the associated email for verification.
     *
     * @param token the expired or invalid JWT
     * @param email the email associated with the token
     * @return a {@link Mono} emitting the new refreshed JWT as a {@link String}
     */
    public Mono<String> refreshToken (String token, String email){
        return client.post()
                .uri(uriBuilder -> uriBuilder.path("/refresh/recreate")
                        .queryParam("token", token)
                        .queryParam("email", email)
                        .build())
                .retrieve()
                .bodyToMono(String.class);
    }

}
