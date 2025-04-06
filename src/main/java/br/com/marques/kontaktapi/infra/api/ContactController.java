package br.com.marques.kontaktapi.infra.api;

import br.com.marques.kontaktapi.app.usecase.CreateContactUseCase;
import br.com.marques.kontaktapi.domain.dto.contact.ContactRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Contact", description = "Contact management")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contact")
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
}
