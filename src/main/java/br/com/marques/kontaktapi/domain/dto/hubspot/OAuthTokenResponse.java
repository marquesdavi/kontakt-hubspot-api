package br.com.marques.kontaktapi.domain.dto.hubspot;

import java.io.Serializable;

public record OAuthTokenResponse(
        String access_token,
        String refresh_token,
        String token_type,
        Integer expires_in,
        String scope
) implements Serializable {
}
