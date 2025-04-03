package br.com.marques.kontaktapi.domain.vo;

import br.com.marques.kontaktapi.exception.ValidationError;
import org.springframework.http.HttpStatusCode;

import java.util.List;


public record ErrorResponse(String message, HttpStatusCode statusCode, List<ValidationError> errors) {
    public ErrorResponse(String message, HttpStatusCode statusCode) {
        this(message, statusCode, null);
    }
}

