package br.com.marques.kontaktapi.service;

import br.com.marques.kontaktapi.config.hubspot.HubspotApiHelper;
import br.com.marques.kontaktapi.domain.dto.hubspot.OAuthCallbackRequest;
import br.com.marques.kontaktapi.domain.dto.hubspot.OAuthTokenResponse;
import br.com.marques.kontaktapi.domain.dto.user.LoginRequest;
import br.com.marques.kontaktapi.domain.dto.user.TokenResponse;
import br.com.marques.kontaktapi.service.gateway.AuthenticationServiceGateway;
import br.com.marques.kontaktapi.service.gateway.ContactServiceGateway;
import br.com.marques.kontaktapi.service.gateway.OAuthServiceGateway;
import br.com.marques.kontaktapi.domain.dto.contact.ContactRequest;
import br.com.marques.kontaktapi.domain.entity.User;
import br.com.marques.kontaktapi.config.resilience.Resilient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactService implements ContactServiceGateway {
    private final AuthenticationServiceGateway<User, LoginRequest, TokenResponse> authenticationService;
    private final OAuthServiceGateway<OAuthCallbackRequest, OAuthTokenResponse> oAuthService;
    private final HubspotApiHelper hubspotApiHelper;

    @Override
    @Resilient(rateLimiter = "hubspotRateLimiter", circuitBreaker = "CircuitBreaker")
    public void createContact(ContactRequest contactRequest) {
        User loggedUser = authenticationService.getAuthenticated();
        Long userId = loggedUser.getId();

        String accessToken = oAuthService.getAccessTokenByUserId(userId);
        if (Objects.isNull(accessToken) || accessToken.isEmpty())
            throw new IllegalStateException("HubSpot access token not available for user " + userId);

        Map<String, Object> requestBody = buildRequestBody(contactRequest);

        Mono<ContactRequest> responseMono = hubspotApiHelper.executePostJsonCall(
                "/crm/v3/objects/contacts",
                requestBody,
                ContactRequest.class,
                accessToken
        );

        responseMono.block();
        log.info("Contact created successfully for user {}", userId);
    }

    @Override
    @Resilient(rateLimiter = "hubspotRateLimiter", circuitBreaker = "CircuitBreaker")
    public Mono<Map<String, Object>> listContacts() {
        User loggedUser = authenticationService.getAuthenticated();
        Long userId = loggedUser.getId();

        String accessToken = oAuthService.getAccessTokenByUserId(userId);
        if (Objects.isNull(accessToken) || accessToken.isEmpty())
            return Mono.error(new IllegalStateException("HubSpot access token not available for user " + userId));

        return hubspotApiHelper.executeGetJsonCall(
                        "/crm/v3/objects/contacts",
                        new ParameterizedTypeReference<Map<String, Object>>() {},
                        accessToken
                )
                .doOnSuccess(response -> log.info("Contacts listed successfully for user {}", userId))
                .doOnError(e -> log.error("Error listing contacts for user {}: {}", userId, e.toString()));
    }

    public Map<String, Object> buildRequestBody(ContactRequest contactRequest) {
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, String> properties = new HashMap<>();
        properties.put("email", contactRequest.email());
        properties.put("firstname", contactRequest.firstName());
        properties.put("lastname", contactRequest.lastName());
        requestBody.put("properties", properties);
        return requestBody;
    }
}
