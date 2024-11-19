package ar.com.l_airline.gateway_microservice.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    public static final List<String> openEndPoints= List.of("/user/insert"
                                                            ,"/auth/token"
                                                            ,"/auth/validate"
                                                            ,"/eureka");

    public Predicate<ServerHttpRequest> isSecured = request ->
            openEndPoints
                    .stream()
                    .noneMatch(uri -> request.getURI()
                            .getPath()
                            .contains(uri)
                    );

}
