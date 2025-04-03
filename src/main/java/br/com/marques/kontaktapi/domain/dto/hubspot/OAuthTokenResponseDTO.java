package br.com.marques.kontaktapi.domain.dto.hubspot;

import java.io.Serializable;

public record OAuthTokenResponseDTO(
        String access_token,
        String refresh_token,
        String token_type,
        Integer expires_in,
        String scope
) implements Serializable {
}
