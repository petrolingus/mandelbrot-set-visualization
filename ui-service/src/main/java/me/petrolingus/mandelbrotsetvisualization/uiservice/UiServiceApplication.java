package me.petrolingus.mandelbrotsetvisualization.uiservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@SpringBootApplication
public class UiServiceApplication {

	@Value("#{environment['READ_TIMEOUT']?:'1000'}")
	private int readTimeout;

	public static void main(String[] args) {
		SpringApplication.run(UiServiceApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
		return restTemplateBuilder
				.setConnectTimeout(Duration.of(1, ChronoUnit.SECONDS))
				.setReadTimeout(Duration.of(readTimeout, ChronoUnit.MILLIS))
				.build();
	}
}
