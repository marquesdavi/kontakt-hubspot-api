package br.com.marques.kontaktapi.application.usecase;

public interface UserAuthenticationUsecase<Entity, Req, Res> {
    Res authenticate(Req request);
    Res generateResponse(Entity user);
}
