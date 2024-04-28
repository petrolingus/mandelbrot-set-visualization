package me.petrolingus.simpleuiservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class UiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UiServiceApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
//                .setConnectTimeout(Duration.of(1000, ChronoUnit.MILLIS))
//                .setReadTimeout(Duration.of(1000, ChronoUnit.MILLIS))
                .build();
    }
}
