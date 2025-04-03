package br.com.marques.kontaktapi.service;

import br.com.marques.kontaktapi.domain.dto.hubspot.OAuthCallbackDTO;
import br.com.marques.kontaktapi.domain.dto.hubspot.OAuthTokenResponseDTO;

public interface HubspotOAuthService {
    String generateAuthorizationUrl();

    OAuthTokenResponseDTO processTokenExchange(OAuthCallbackDTO callbackDto);

    void refreshAccessToken(String refreshToken);
}
