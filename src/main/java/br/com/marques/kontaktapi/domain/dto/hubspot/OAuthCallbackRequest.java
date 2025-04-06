package br.com.marques.kontaktapi.domain.dto.hubspot;

import java.io.Serializable;

public record OAuthCallbackRequest(String code, String state) implements Serializable {
}
