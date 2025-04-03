package br.com.marques.kontaktapi.domain.dto.user;

public record TokenResponse(String accessToken, Long expiresIn) {
}
