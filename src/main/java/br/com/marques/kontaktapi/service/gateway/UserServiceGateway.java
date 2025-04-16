package br.com.marques.kontaktapi.service.gateway;

import java.util.List;

public interface UserServiceGateway<Entity, Req> {
    void create(Req dto);
    List<Entity> list();
    Entity findByEmail(String email);
    Entity findByIdOrElseThrow(Long id);
}
