package br.com.marques.kontaktapi.infra.service;

import br.com.marques.kontaktapi.app.usecase.CreateContactUseCase;
import br.com.marques.kontaktapi.app.usecase.HubspotTokenUsecase;
import br.com.marques.kontaktapi.app.usecase.UserCrudUsecase;
import br.com.marques.kontaktapi.domain.dto.contact.ContactRequest;
import br.com.marques.kontaktapi.domain.entity.User;
import br.com.marques.kontaktapi.infra.external.HubspotApiHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        ContactRequest createdContact = responseMono.block();
        log.info("Contact created successfully for user {}: {}", userId, createdContact);
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
