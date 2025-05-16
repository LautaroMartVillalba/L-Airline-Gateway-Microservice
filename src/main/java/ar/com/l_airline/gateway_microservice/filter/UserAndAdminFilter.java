package ar.com.l_airline.gateway_microservice.filter;

import ar.com.l_airline.gateway_microservice.cookies.CookieService;
import ar.com.l_airline.gateway_microservice.cookies.UserMicroserviceQueries;
import ar.com.l_airline.gateway_microservice.exception_handler.custom_exceptions.AccessDeniedException;
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

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            if (validator.isSecured.test(exchange.getRequest())){
                HttpCookie cookie = exchange.getRequest().getCookies().get("jwt").getFirst();

                String cookieValue = cookie.getValue();
                String cookieName = cookie.getName();

                try {
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

                        if (!jwtUtil.isAdmin(refresh)){
                            throw new AccessDeniedException();
                        }

                        cookieService.createCookie(cookieName, refresh, Duration.ofDays(3), exchange);

                        return chain.filter(mutatedExchange);
                    });
                }

                return chain.filter(exchange);
            }else if (exchange.getRequest().getURI().getPath().contains("/auth/token")){
                String email = exchange.getRequest().getQueryParams().getFirst("email");
                String pass = exchange.getRequest().getQueryParams().getFirst("password");

                return queries.createToken(email, pass).flatMap(s -> {
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
