package br.com.marques.kontaktapi.infrastructure.service;

import br.com.marques.kontaktapi.application.usecase.ProcessContactCreationWebhookUseCase;
import br.com.marques.kontaktapi.domain.dto.contact.ContactCreationEventRequest;
import br.com.marques.kontaktapi.domain.entity.ContactCreationEventEntity;
import br.com.marques.kontaktapi.infrastructure.persistence.ContactCreationEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactWebhookService implements ProcessContactCreationWebhookUseCase {
    private final ContactCreationEventRepository repository;

    @Override
    public void process(List<ContactCreationEventRequest> events) {
        log.info("Processing contact creation events");

        List<ContactCreationEventEntity> persistableEvents = events.stream()
                .map(ContactCreationEventEntity::fromRequest)
                .toList();

        repository.saveAll(persistableEvents);

        log.info("Contact creation events processed successfully");
    }
}
