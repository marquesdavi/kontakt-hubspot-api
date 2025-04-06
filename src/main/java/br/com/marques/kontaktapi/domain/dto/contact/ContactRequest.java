package br.com.marques.kontaktapi.domain.dto.contact;

public record ContactRequest(
        String email,
        String firstName,
        String lastName
) {
}
