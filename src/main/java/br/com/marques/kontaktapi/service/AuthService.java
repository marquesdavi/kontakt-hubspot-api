package br.com.marques.kontaktapi.service;

import br.com.marques.kontaktapi.domain.dto.user.LoginRequest;
import br.com.marques.kontaktapi.domain.dto.user.TokenResponse;
import br.com.marques.kontaktapi.domain.entity.User;

public interface AuthService {
    TokenResponse authenticate(LoginRequest request);
    TokenResponse generateResponse(User user);
}
