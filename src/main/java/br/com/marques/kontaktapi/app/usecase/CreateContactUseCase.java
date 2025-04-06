package br.com.marques.kontaktapi.app.usecase;

import br.com.marques.kontaktapi.domain.dto.contact.ContactRequest;

public interface CreateContactUseCase {
    void createContact(ContactRequest contactRequest);
}
