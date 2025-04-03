package br.com.marques.kontaktapi.controller;

import br.com.marques.kontaktapi.domain.dto.hubspot.OAuthCallbackDTO;
import br.com.marques.kontaktapi.domain.dto.hubspot.OAuthTokenResponseDTO;
import br.com.marques.kontaktapi.service.HubspotOAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Hubspot Authentication", description = "Hubspot Authentication management")
public class HubspotOAuthController {
    private final HubspotOAuthService authService;

    @Operation(summary = "Returns an Authorization URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @GetMapping("/url")
    public ResponseEntity<String> getAuthorizationUrl() {
        String authUrl = authService.generateAuthorizationUrl();
        return ResponseEntity.ok(authUrl);
    }

    @Operation(summary = "Handle token exchange")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @GetMapping(value = "/callback", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView doTokenExchange(@RequestParam("code") String code,
                                        @RequestParam(value = "state", required = false) String state) {
        OAuthCallbackDTO callbackDto = new OAuthCallbackDTO(code, state);
        try {
            OAuthTokenResponseDTO tokenResponse = authService.processTokenExchange(callbackDto);
            ModelAndView mav = new ModelAndView("authentication-callback");
            mav.addObject("messageType", "success");
            mav.addObject("message", "Authentication successful. You can close this window.");
            return mav;
        } catch (Exception ex) {
            ModelAndView mav = new ModelAndView("authentication-callback");
            mav.addObject("messageType", "error");
            mav.addObject("message", "Authentication error. Please try again.");
            return mav;
        }
    }
}
