package jz.pk.evcm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AppConfig {



    @Bean
    public RestClient.Builder restClientBuilder() {
        // REST CIENT DOCS: https://docs.spring.io/spring-framework/reference/integration/rest-clients.html
        return RestClient.builder();
    }

}
