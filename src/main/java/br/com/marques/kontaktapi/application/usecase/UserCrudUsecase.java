package br.com.marques.kontaktapi.application.usecase;

import java.util.List;

public interface UserCrudUsecase<Entity, Req> {
    void create(Req dto);
    List<Entity> list();
    Entity findByEmail(String email);
    Entity getLogged();
}
