package br.com.marques.kontaktapi.domain.dto.contact;

public record ContactDTO(
        String email,
        String firstName,
        String lastName
) {
}
