package br.com.marques.kontaktapi.app.usecase;

import br.com.marques.kontaktapi.domain.dto.user.LoginRequest;
import br.com.marques.kontaktapi.domain.dto.user.TokenResponse;
import br.com.marques.kontaktapi.domain.entity.User;

public interface UserAuthenticationUsecase<Entity, Req, Res> {
    Res authenticate(Req request);
    Res generateResponse(Entity user);
}
