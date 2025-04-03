package br.com.marques.kontaktapi.config.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class HubspotWebClient {
    @Value("${hubspot.api.base-url}")
    private String baseUrl;

    @Bean
    public WebClient buildHubspotWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
