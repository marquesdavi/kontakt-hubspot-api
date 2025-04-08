package br.com.marques.kontaktapi.infrastructure.api;

import br.com.marques.kontaktapi.application.usecase.ProcessContactCreationWebhookUseCase;
import br.com.marques.kontaktapi.domain.dto.contact.ContactCreationEventRequest;
import br.com.marques.kontaktapi.infrastructure.config.security.validation.HmacValidation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Webhook", description = "Contact Webhook management")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hubspot/webhook")
public class ContactWebhookController {
    private final ProcessContactCreationWebhookUseCase processContactCreationWebhookUseCase;

    @HmacValidation
    @Operation(summary = "Process contact creation webhook events")
    @PostMapping(value = "/contact", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> handleContactCreationWebhook(@RequestBody List<ContactCreationEventRequest> events) {
        processContactCreationWebhookUseCase.process(events);
        return ResponseEntity.ok().build();
    }
}
