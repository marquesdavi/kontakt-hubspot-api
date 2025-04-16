package br.com.marques.kontaktapi.service.gateway;

public interface AuthenticationServiceGateway<Entity, Req, Res> {
    Res authenticate(Req request);
    Res generateResponse(Entity user);
    Entity getAuthenticated();
}
