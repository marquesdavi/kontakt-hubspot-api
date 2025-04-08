package br.com.marques.kontaktapi.application.usecase;

import br.com.marques.kontaktapi.domain.dto.contact.ContactRequest;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface CreateContactUseCase {
    void createContact(ContactRequest contactRequest);
    Mono<Map<String, Object>> listContacts();

}
