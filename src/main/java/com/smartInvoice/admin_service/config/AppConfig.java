package com.smartInvoice.admin_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(AdminProperties.class)
public class AppConfig {
	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}

	@Bean
	WebClient.Builder webClientBuilder() {
		return WebClient.builder();
	}

	@Bean
	ObjectMapper objectMapper() {
		return new ObjectMapper();
	}
}
