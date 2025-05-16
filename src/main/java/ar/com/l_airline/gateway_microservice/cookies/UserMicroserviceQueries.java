package ar.com.l_airline.gateway_microservice.cookies;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class UserMicroserviceQueries {

    private final WebClient client  = WebClient.builder()
                                        .baseUrl("http://localhost:9000")
                                        .build();

    public Mono<String> createToken(String email, String password){
        return client.post()
                .uri(uriBuilder -> uriBuilder.path("/auth/token")
                        .queryParam("email", email)
                        .queryParam("password", password)
                        .build())
                .retrieve()
                .bodyToMono(String.class);
    }

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
