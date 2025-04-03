package br.com.marques.kontaktapi.domain.dto.hubspot;

import java.io.Serializable;

public record OAuthCallbackDTO(String code, String state) implements Serializable {
}
