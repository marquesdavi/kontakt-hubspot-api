package br.com.marques.kontaktapi.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AlreadyExistsException extends ResponseStatusException {
    public AlreadyExistsException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
