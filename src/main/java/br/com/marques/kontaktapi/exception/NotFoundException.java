package br.com.marques.kontaktapi.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends GenericException {
    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
