package ar.com.l_airline.gateway_microservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestConfig {

    @Bean
    public RestTemplate template(){
        return new RestTemplate();
    }

}
