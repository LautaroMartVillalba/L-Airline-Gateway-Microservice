package ar.com.l_airline.gateway_microservice.filter;

import ar.com.l_airline.gateway_microservice.util.JwtUtil;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFilter  extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;
    @Autowired
    private JwtUtil jwt;

    public AuthenticationFilter(){
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            if (validator.isSecured.test(exchange.getRequest())){
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)){
                    throw new RuntimeException("Vos no tenés el header en orden.");
                }

                String authenticationHeader = exchange.getRequest()
                        .getHeaders()
                        .get(org.springframework.http.HttpHeaders.AUTHORIZATION)
                        .get(0);
                if (authenticationHeader != null && authenticationHeader.startsWith("Bearer ")){
                    authenticationHeader = authenticationHeader.substring(7);
                }
                try {
                    jwt.validateToken(authenticationHeader);
                }catch (Exception e){
                    throw new RuntimeException("No tenés acceso, maestro. Andá pa' 'lla.");
                }
            }
            return chain.filter(exchange);
        });
    }

    public static class Config{}

}
