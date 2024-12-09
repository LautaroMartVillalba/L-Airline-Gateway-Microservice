package ar.com.l_airline.gateway_microservice.exception_handler;

import ar.com.l_airline.gateway_microservice.exception_handler.custom_exceptions.AccessDeniedException;
import ar.com.l_airline.gateway_microservice.exception_handler.custom_exceptions.InvalidTokenException;
import ar.com.l_airline.gateway_microservice.exception_handler.custom_exceptions.TokenExpiredException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.ConnectException;

@ControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<ExceptionDTO> accessDeniedExcHandler(){
        ExceptionDTO dto = ExceptionDTO.builder()
                .code(HttpStatusCode.valueOf(403))
                .message("You has no permissions to access.").build();

        return ResponseEntity.status(dto.getCode()).body(dto);
    }

    @ExceptionHandler(value = InvalidTokenException.class)
    public ResponseEntity<ExceptionDTO> invalidTokenExcHandler(){
        ExceptionDTO dto = ExceptionDTO.builder()
                .code(HttpStatusCode.valueOf(400))
                .message("Invalid token. What are you trying?").build();

        return ResponseEntity.status(dto.getCode()).body(dto);
    }

    @ExceptionHandler(value = SignatureException.class)
    public ResponseEntity<ExceptionDTO> invalidSignatureExcHandler(){
        ExceptionDTO dto = ExceptionDTO.builder()
                .code(HttpStatusCode.valueOf(400))
                .message("Invalid token. What are you trying?").build();

        return ResponseEntity.status(dto.getCode()).body(dto);
    }

    @ExceptionHandler(value = TokenExpiredException.class)
    public ResponseEntity<ExceptionDTO> tokenExpiredException(){
        ExceptionDTO dto = ExceptionDTO.builder()
                .code(HttpStatusCode.valueOf(408))
                .message("So slow! Your Token has expired.").build();

        return ResponseEntity.status(dto.getCode()).body(dto);
    }

    @ExceptionHandler(value = ExpiredJwtException.class)
    public ResponseEntity<ExceptionDTO> expiredJwtException(){
        ExceptionDTO dto = ExceptionDTO.builder()
                .code(HttpStatusCode.valueOf(403))
                .message("So slow! Your Token has expired.").build();

        return ResponseEntity.status(dto.getCode()).body(dto);
    }

    @ExceptionHandler(value = ConnectException.class)
    public ResponseEntity<ExceptionDTO> connectionExHandler(){
        ExceptionDTO dto = ExceptionDTO.builder()
                .code(HttpStatusCode.valueOf(404))
                .message("Sorry! We're having problems with our server connection. Please try again later.").build();

        return ResponseEntity.status(dto.getCode()).body(dto);
    }
}
