package ar.com.l_airline.gateway_microservice.cookies;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.time.Duration;

@Component
public class CookieService {

    public void createCookie(String name, String value, Duration time, ServerWebExchange exchange){

        ResponseCookie cookie = ResponseCookie
                .from(name, value)
                .maxAge(time)
                .path("/")
                .secure(false)
                .httpOnly(false).build();

        exchange.getResponse().addCookie(cookie);
    }

    public void deleteCookie(String name, ServerWebExchange exchange){
        ResponseCookie cookie = ResponseCookie
                .from(name)
                .maxAge(0)
                .path("/").build();

        exchange.getResponse().addCookie(cookie);
    }

}
