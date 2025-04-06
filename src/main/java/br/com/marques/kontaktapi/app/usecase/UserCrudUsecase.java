package br.com.marques.kontaktapi.app.usecase;

import br.com.marques.kontaktapi.domain.dto.user.RegisterRequest;
import br.com.marques.kontaktapi.domain.entity.User;

import java.util.List;

public interface UserCrudUsecase<Entity, Req> {
    void create(Req dto);
    List<Entity> list();
    Entity findByEmail(String email);
    Entity getLogged();
}
