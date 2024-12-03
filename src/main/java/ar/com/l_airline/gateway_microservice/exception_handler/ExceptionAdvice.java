package ar.com.l_airline.gateway_microservice.exception_handler;

import ar.com.l_airline.gateway_microservice.exception_handler.custom_exceptions.AccessDeniedException;
import ar.com.l_airline.gateway_microservice.exception_handler.custom_exceptions.InvalidTokenException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<ExceptionDTO> accessDeniedExcHandler(){
        ExceptionDTO dto = ExceptionDTO.builder()
                .code(HttpStatusCode.valueOf(403))
                .message("You has no permissions to access.").build();

        return ResponseEntity.status(403).body(dto);
    }

    @ExceptionHandler(value = InvalidTokenException.class)
    public ResponseEntity<ExceptionDTO> invalidTokenExcHandler(){
        ExceptionDTO dto = ExceptionDTO.builder()
                .code(HttpStatusCode.valueOf(400))
                .message("Invalid token. What are you trying?|").build();

        return ResponseEntity.status(400).body(dto);
    }

    //todo implement jwtException and signatureException

}
