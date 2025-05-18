package ar.com.l_airline.gateway_microservice.filter;

import ar.com.l_airline.gateway_microservice.util.CookieService;
import ar.com.l_airline.gateway_microservice.internal_communication.UserMicroserviceQueries;
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
 * Gateway filter responsible for managing JWT authentication and automatic token refresh.
 * This version of the filter does not enforce role-based access control (e.g., ADMIN check),
 * but still ensures that secured routes are accessed only with valid tokens.
 *
 * Responsibilities:
 * - Validates JWTs retrieved from cookies for secured routes.
 * - Attempts to refresh expired or invalid tokens using user microservice queries.
 * - Injects refreshed tokens into request headers.
 * - Creates and updates JWT cookies accordingly.
 * - Handles authentication requests to the /auth/token endpoint and issues new tokens.
 */
@Component
public class UserAndAdminFilter extends AbstractGatewayFilterFactory<UserAndAdminFilter.Config> {

    @Autowired
    private RouteValidator validator;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserMicroserviceQueries queries;
    @Autowired
    private CookieService cookieService;

    public UserAndAdminFilter(){
        super(Config.class);
    }

    /**
     * Applies the core logic of the filter to incoming requests.
     * Secured routes require valid JWTs, which are validated or refreshed automatically.
     * Authentication routes generate new tokens and set them as cookies.
     *
     * @param config the configuration for this filter (not currently utilized)
     * @return the configured {@link GatewayFilter}
     */
    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            // Handle secured routes
            if (validator.isSecured.test(exchange.getRequest())){
                HttpCookie cookie = exchange.getRequest().getCookies().get("jwt").getFirst();

                String cookieValue = cookie.getValue();
                String cookieName = cookie.getName();

                try {
                    // Attempt to validate the existing token
                    jwtUtil.validateToken(cookieValue);
                }catch (Exception e){
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

                        // Update the cookie with the refreshed token
                        cookieService.createCookie(cookieName, refresh, Duration.ofDays(3), exchange);

                        return chain.filter(mutatedExchange);
                    });
                }
                return chain.filter(exchange);
            // Handle token creation on /auth/token endpoint
            }else if (exchange.getRequest().getURI().getPath().contains("/auth/token")){
                String email = exchange.getRequest().getQueryParams().getFirst("email");
                String pass = exchange.getRequest().getQueryParams().getFirst("password");

                return queries.createToken(email, pass).flatMap(s -> {
                    // Set the JWT in a cookie upon successful authentication
                    cookieService.createCookie("jwt", s, Duration.ofDays(3), exchange);
                    return chain.filter(exchange);
                });
            }
            return chain.filter(exchange);
        });
    }

    public static class Config{
        public Config() {
        }
    }
}
