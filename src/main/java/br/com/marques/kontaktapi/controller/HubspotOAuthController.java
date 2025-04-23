package br.com.marques.kontaktapi.controller;

import br.com.marques.kontaktapi.domain.dto.hubspot.OAuthCallbackRequest;
import br.com.marques.kontaktapi.service.gateway.OAuthServiceGateway;
import br.com.marques.kontaktapi.domain.dto.hubspot.OAuthTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hubspot")
@Tag(name = "Hubspot Authentication", description = "Hubspot Authentication management")
public class HubspotOAuthController {
    private final OAuthServiceGateway<OAuthCallbackRequest, OAuthTokenResponse> oAuthService;

    @Operation(summary = "Returns an Authorization URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @GetMapping("/url")
    public ResponseEntity<String> getAuthorizationUrl() {
        String authUrl = oAuthService.generateAuthorizationUrl();
        return ResponseEntity.ok(authUrl);
    }

    @GetMapping(value = "/callback", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView handleTokenExchange(@RequestParam("code") String code,
                                            @RequestParam(value = "state", required = false) String state) {
        try {
            oAuthService.processTokenExchange(new OAuthCallbackRequest(code, state));
            return createView("success", "Authentication successful. You can close this window.");
        } catch (Exception ex) {
            log.error("Error in token exchange: {}", ex.getMessage());
            return createView("error", "Authentication error. Please try again.");
        }
    }

    private ModelAndView createView(String messageType, String message) {
        ModelAndView mav = new ModelAndView("authentication-callback");
        mav.addObject("messageType", messageType);
        mav.addObject("message", message);
        return mav;
    }
}
