package ar.com.l_airline.gateway_microservice.filter;

import ar.com.l_airline.gateway_microservice.util.CookieService;
import ar.com.l_airline.gateway_microservice.internal_communication.UserMicroserviceQueries;
import ar.com.l_airline.gateway_microservice.exception_handling.controller_advice.custom_exceptions.AccessDeniedException;
import ar.com.l_airline.gateway_microservice.util.JwtUtil;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.time.Duration;

/**
 * A custom Spring Cloud Gateway filter that handles JWT-based authentication and authorization
 * for secured routes. It ensures that users accessing secured routes have valid tokens and
 * ADMIN privileges when required.
 *
 * Responsibilities:
 * - Validates JWTs from cookies.
 * - Refreshes tokens if expired.
 * - Injects refreshed tokens into the request header.
 * - Verifies an ADMIN role for sensitive routes.
 * - Creates JWT cookies after successful authentication.
 */
@Component
public class AdminFilter  extends AbstractGatewayFilterFactory<AdminFilter.Config> {

    @Autowired
    private RouteValidator validator;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserMicroserviceQueries queries;
    @Autowired
    private CookieService cookieService;

    public AdminFilter(){
        super(Config.class);
    }

    /**
     * Applies the filtering logic based on the provided configuration.
     * Handles token validation, token refreshing, role-based access control,
     * and JWT cookie creation.
     *
     * @param config the configuration for this filter (currently unused)
     * @return the configured {@link GatewayFilter}
     */
    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            // Check if the current request requires authentication
            if (validator.isSecured.test(exchange.getRequest())){
                HttpCookie cookie = exchange.getRequest().getCookies().get("jwt").getFirst();

                String cookieValue = cookie.getValue();
                String cookieName = cookie.getName();

                try {
                    // Attempt to validate the token
                    jwtUtil.validateToken(cookieValue);
                }catch (Exception e){
                    // If validation fails, try to refresh the token
                    return queries.refreshToken(cookieValue, jwtUtil.getEmail(cookieValue)).flatMap(refresh ->{
                        ServerHttpRequest newRequest = exchange
                                .getRequest()
                                .mutate()
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + refresh)
                                .build();

                        ServerWebExchange mutatedExchange = exchange
                                .mutate()
                                .request(newRequest)
                                .build();

                        if (!jwtUtil.isAdmin(refresh)){
                            throw new AccessDeniedException();
                        }

                        // Create a new cookie with the refreshed token
                        cookieService.createCookie(cookieName, refresh, Duration.ofDays(3), exchange);

                        return chain.filter(mutatedExchange);
                    });
                }
                if (!jwtUtil.isAdmin(cookieValue)){
                    throw new AccessDeniedException();
                }
                // Proceed with the original exchange if the token is valid
                return chain.filter(exchange);
            }
            // Handle token creation at /auth/token endpoint
            else if (exchange.getRequest().getURI().getPath().contains("/auth/token")){
                String email = exchange.getRequest().getQueryParams().getFirst("email");
                String pass = exchange.getRequest().getQueryParams().getFirst("password");

                return queries.createToken(email, pass).flatMap(s -> {
                    cookieService.createCookie("jwt", s, Duration.ofDays(3), exchange);
                    return chain.filter(exchange);
                });
            }
            // Proceed with request if not secured or matched by other conditions
            return chain.filter(exchange);
        });
    }

    public static class Config{}

}
