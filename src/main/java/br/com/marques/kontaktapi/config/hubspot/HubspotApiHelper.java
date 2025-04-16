package br.com.marques.kontaktapi.config.hubspot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubspotApiHelper {

    private final WebClient webClient;

    @Value("${hubspot.client.id}")
    private String clientId;
    @Value("${hubspot.client.secret}")
    private String clientSecret;
    @Value("${hubspot.redirect-uri}")
    private String redirectUri;
    @Value("${hubspot.oauth.authorization-url}")
    private String authorizationUrl;

    public MultiValueMap<String, String> buildCallParameters(String grantType) {
        var parameters = new org.springframework.util.LinkedMultiValueMap<String, String>();
        parameters.add("grant_type", grantType);
        parameters.add("client_id", clientId);
        parameters.add("client_secret", clientSecret);
        parameters.add("redirect_uri", redirectUri);
        return parameters;
    }

    public <R> Mono<R> executeCall(String endpoint, MultiValueMap<String, String> params, Class<R> responseType) {
        log.info("Executing client call");
        return webClient.post()
                .uri(endpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(params))
                .retrieve()
                .bodyToMono(responseType)
                .doOnSuccess(r -> log.info("Client call executed successfully"))
                .doOnError(e -> log.error("Error executing client call: {}", e.getMessage()));
    }

    public String generateAuthorizationUrl(String scopes, String state) {
        return authorizationUrl +
                "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                "&scope=" + URLEncoder.encode(scopes, StandardCharsets.UTF_8) +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&state=" + state;
    }

    public <R> Mono<R> executePostJsonCall(String endpoint, Object requestBody, Class<R> responseType, String accessToken) {
        log.info("Executing JSON call to endpoint: {}", endpoint);
        return webClient.post()
                .uri(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(responseType)
                .doOnSuccess(r -> log.info("JSON call executed successfully"))
                .doOnError(e -> log.error("Error executing JSON call: {}", e.getMessage()));
    }

    public <R> Mono<R> executeGetJsonCall(String endpoint, ParameterizedTypeReference<R> typeRef, String accessToken) {
        log.info("Executing GET JSON call to endpoint: {}", endpoint);
        return webClient.get()
                .uri(endpoint)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(typeRef)
                .doOnSuccess(r -> log.info("GET JSON call executed successfully"))
                .doOnError(e -> log.error("Error executing GET JSON call: {}", e.toString()));
    }
}
