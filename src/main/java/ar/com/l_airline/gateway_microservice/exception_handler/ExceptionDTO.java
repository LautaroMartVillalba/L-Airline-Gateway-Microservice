package ar.com.l_airline.gateway_microservice.exception_handler;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatusCode;

@Builder
@Data
public class ExceptionDTO {
    private HttpStatusCode code;
    private String message;
}
