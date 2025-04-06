package br.com.marques.kontaktapi.infra.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

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
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("grant_type", grantType);
        parameters.add("client_id", clientId);
        parameters.add("client_secret", clientSecret);
        parameters.add("redirect_uri", redirectUri);
        return parameters;
    }

    public <R> Mono<R> executeCall(String endpoint, MultiValueMap<String, String> params, Class<R> responseType) {
        log.info("Executing client call: {}", params.toString());
        Mono<R> response = webClient.post()
                .uri(endpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(params))
                .retrieve()
                .bodyToMono(responseType);

        log.info("Client call executed successfully");
        return response;
    }

    public String generateAuthorizationUrl(String scopes, String state) {
        return authorizationUrl +
                "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                "&scope=" + URLEncoder.encode(scopes, StandardCharsets.UTF_8) +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&state=" + state;
    }
}
