package br.com.marques.kontaktapi.infra.service;

import br.com.marques.kontaktapi.app.usecase.ProcessContactCreationWebhookUseCase;
import br.com.marques.kontaktapi.domain.dto.contact.ContactCreationEventRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactWebhookService implements ProcessContactCreationWebhookUseCase {

    @Override
    public void process(List<ContactCreationEventRequest> events) {
        events.forEach(event -> {
            log.info("Processing contact creation event: objectId={}, subscriptionType={}, occurredAt={}, attemptNumber={}",
                    event.objectId(), event.subscriptionType(), event.occurredAt(), event.attemptNumber());
        });
    }
}
