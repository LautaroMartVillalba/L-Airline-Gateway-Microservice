package ar.com.l_airline.gateway_microservice.handling;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import java.util.Map;

@Component
public class AbstractHandling extends AbstractErrorWebExceptionHandler {

    public AbstractHandling(ErrorAttributes errorAttributes,
                            ApplicationContext applicationContext,
                            ServerCodecConfigurer serverCodecConfigurer) {
        super(errorAttributes, new WebProperties.Resources(), applicationContext);
        super.setMessageWriters(serverCodecConfigurer.getWriters());
        super.setMessageReaders(serverCodecConfigurer.getReaders());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::errorResponse);
    }

    private Mono<ServerResponse> errorResponse(ServerRequest request){
        ErrorAttributeOptions attributeOptions = ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE);
        Map<String, Object> errorProperties = getErrorAttributes(request, attributeOptions);

        Throwable throwable = getError(request);

        HttpStatusCode httpStatusCode = this.determineStatusCode(throwable);

        return ServerResponse.status(httpStatusCode)
                             .contentType(MediaType.APPLICATION_JSON)
                             .body(BodyInserters.fromValue(errorProperties));
    }

    private HttpStatusCode determineStatusCode(Throwable throwable){
        if (throwable instanceof ResponseStatusException){
            return ((ResponseStatusException) throwable).getStatusCode();
        }else {
            return HttpStatusCode.valueOf(500);
        }
    }
}
