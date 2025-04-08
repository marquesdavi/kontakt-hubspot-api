package br.com.marques.kontaktapi.infrastructure.api;

import br.com.marques.kontaktapi.application.usecase.CreateContactUseCase;
import br.com.marques.kontaktapi.domain.dto.contact.ContactRequest;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@Tag(name = "Contact", description = "Contact management")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contact")
@RateLimiter(name = "RateLimiter", fallbackMethod = "rateLimiterFallback")
public class ContactController {

    private final CreateContactUseCase createContactUseCase;

    @Operation(summary = "Creates a new contact in HubSpot")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Contact created"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<Void> createContact(@RequestBody ContactRequest contactRequest) {
        createContactUseCase.createContact(contactRequest);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Lists contacts from HubSpot")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contacts retrieved"),
            @ApiResponse(responseCode = "400", description = "Error retrieving contacts")
    })
    @GetMapping
    public Mono<ResponseEntity<Map<String, Object>>> listContacts() {
        return createContactUseCase.listContacts()
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().build()));
    }

    public String rateLimiterFallback(Throwable t) {
        return "Too many requests - please try again later.";
    }
}
