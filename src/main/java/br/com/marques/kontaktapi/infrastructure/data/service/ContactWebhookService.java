package br.com.marques.kontaktapi.infrastructure.data.service;

import br.com.marques.kontaktapi.application.usecase.ProcessWebhookUseCase;
import br.com.marques.kontaktapi.domain.dto.contact.ContactCreationEventRequest;
import br.com.marques.kontaktapi.domain.entity.ContactCreationEventEntity;
import br.com.marques.kontaktapi.infrastructure.config.resilience.Resilient;
import br.com.marques.kontaktapi.infrastructure.data.persistence.ContactCreationEventRepository;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactWebhookService
        implements ProcessWebhookUseCase<ContactCreationEventEntity, ContactCreationEventRequest> {

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

    @Override
    @Resilient(rateLimiter = "hubspotRateLimiter", circuitBreaker = "CircuitBreaker", fallbackMethod = "fallback")
    public Page<ContactCreationEventEntity> listEvents(@PositiveOrZero Integer page,
                                                       @Positive Integer size) {
        PageRequest pageable = PageRequest.of(page, size);
        return repository.findAll(pageable);
    }

    public Page<ContactCreationEventEntity> fallback(Integer page, Integer size, Throwable t) {
        log.warn("Hubspot API rate limit exceeded. Please try again later.");
        throw new RuntimeException("Hubspot API rate limit exceeded. Please try again later.", t);
    }
}
