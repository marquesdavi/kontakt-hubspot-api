package br.com.marques.kontaktapi.service.gateway;

import br.com.marques.kontaktapi.domain.dto.contact.ContactRequest;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface ContactServiceGateway {
    void createContact(ContactRequest contactRequest);
    Mono<Map<String, Object>> listContacts();

}
