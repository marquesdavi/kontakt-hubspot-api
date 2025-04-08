package br.com.marques.kontaktapi.infrastructure.api.controller;

import br.com.marques.kontaktapi.application.usecase.ProcessWebhookUseCase;
import br.com.marques.kontaktapi.domain.dto.contact.ContactCreationEventRequest;
import br.com.marques.kontaktapi.domain.entity.ContactCreationEventEntity;
import br.com.marques.kontaktapi.infrastructure.config.security.validation.HmacValidation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Webhook", description = "Contact Webhook management")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hubspot/webhook")
public class WebhookController {

    private final ProcessWebhookUseCase<ContactCreationEventEntity, ContactCreationEventRequest> processWebhookUseCase;

    @HmacValidation
    @Operation(summary = "Process contact creation webhook events")
    @PostMapping(value = "/contact", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> handleContactCreationWebhook(@RequestBody List<ContactCreationEventRequest> events) {
        processWebhookUseCase.process(events);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Retrieves contact creation webhook events with pagination")
    @GetMapping(value = "/contact")
    public ResponseEntity<Page<ContactCreationEventEntity>> listContactCreationEvents(
                                        @RequestParam(defaultValue = "0") Integer pageNumber,
                                        @RequestParam(defaultValue = "10") Integer size) {
        Page<ContactCreationEventEntity> page = processWebhookUseCase.listEvents(pageNumber, size);
        return ResponseEntity.ok(page);
    }
}
