package br.com.marques.kontaktapi.application.usecase;

import br.com.marques.kontaktapi.domain.dto.contact.ContactCreationEventRequest;

import java.util.List;

public interface ProcessContactCreationWebhookUseCase {
    void process(List<ContactCreationEventRequest> events);
}
