package br.com.marques.kontaktapi.infrastructure.service;

import br.com.marques.kontaktapi.application.usecase.CreateContactUseCase;
import br.com.marques.kontaktapi.application.usecase.HubspotTokenUsecase;
import br.com.marques.kontaktapi.application.usecase.UserCrudUsecase;
import br.com.marques.kontaktapi.domain.dto.contact.ContactRequest;
import br.com.marques.kontaktapi.domain.entity.User;
import br.com.marques.kontaktapi.infrastructure.config.resilience.Resilient;
import br.com.marques.kontaktapi.infrastructure.external.HubspotApiHelper;
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
public class ContactService implements CreateContactUseCase {

    private final UserCrudUsecase<User, ?> userCrudUsecase;
    private final HubspotTokenUsecase<?, ?> hubspotTokenUsecase;
    private final HubspotApiHelper hubspotApiHelper;

    @Override
    @Resilient(rateLimiter = "hubspotRateLimiter", circuitBreaker = "CircuitBreaker", fallbackMethod = "fallback")
    public void createContact(ContactRequest contactRequest) {
        User loggedUser = userCrudUsecase.getLogged();
        Long userId = loggedUser.getId();

        String accessToken = hubspotTokenUsecase.getAccessTokenByUserId(userId);
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
    @Resilient(rateLimiter = "hubspotRateLimiter", circuitBreaker = "CircuitBreaker", fallbackMethod = "fallback")
    public Mono<Map<String, Object>> listContacts() {
        User loggedUser = userCrudUsecase.getLogged();
        Long userId = loggedUser.getId();

        String accessToken = hubspotTokenUsecase.getAccessTokenByUserId(userId);
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

    public void fallback(Throwable t) {
        log.warn("Hubspot API rate limit exceeded. Please try again later.");
        throw new RuntimeException("Hubspot API rate limit exceeded. Please try again later.", t);
    }

    Map<String, Object> buildRequestBody(ContactRequest contactRequest) {
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, String> properties = new HashMap<>();
        properties.put("email", contactRequest.email());
        properties.put("firstname", contactRequest.firstName());
        properties.put("lastname", contactRequest.lastName());
        requestBody.put("properties", properties);
        return requestBody;
    }
}
