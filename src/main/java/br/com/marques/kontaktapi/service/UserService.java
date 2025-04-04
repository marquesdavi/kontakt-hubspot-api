package br.com.marques.kontaktapi.service;

import br.com.marques.kontaktapi.domain.dto.user.RegisterRequest;
import br.com.marques.kontaktapi.domain.entity.User;

import java.util.List;

public interface UserService {
    void create(RegisterRequest dto);
    List<User> list();
    User findByEmail(String email);
    User getLogged();
}
