package ar.com.l_airline.gateway_microservice.util;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.time.Duration;
/**
 * Component responsible for managing HTTP cookies in a reactive Spring WebFlux context.
 * Provides utility methods for creating and deleting cookies in the HTTP response.
 */
@Component
public class CookieService {

    /**
     * Creates and adds a cookie to the HTTP response.
     *
     * @param name     the name of the cookie
     * @param value    the value of the cookie
     * @param time     the duration (max age) the cookie will be valid for
     * @param exchange the {@link ServerWebExchange} representing the current web request and response
     */
    public void createCookie(String name, String value, Duration time, ServerWebExchange exchange){

        ResponseCookie cookie = ResponseCookie
                .from(name, value)
                .maxAge(time)
                .path("/")
                .secure(false)
                .httpOnly(false).build();

        exchange.getResponse().addCookie(cookie);
    }

    /**
     * Deletes a cookie by setting its max age to zero and adding it to the HTTP response.
     *
     * @param name     the name of the cookie to be deleted
     * @param exchange the {@link ServerWebExchange} representing the current web request and response
     */
    public void deleteCookie(String name, ServerWebExchange exchange){
        ResponseCookie cookie = ResponseCookie
                .from(name)
                .maxAge(0)
                .path("/").build();

        exchange.getResponse().addCookie(cookie);
    }

}
