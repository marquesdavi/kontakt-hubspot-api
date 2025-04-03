package br.com.marques.kontaktapi.service;

import br.com.marques.kontaktapi.domain.entity.Role;

public interface RoleService {
    Role findByName(String name);
}
