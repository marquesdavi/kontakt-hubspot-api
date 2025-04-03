package br.com.marques.kontaktapi.service;

import br.com.marques.kontaktapi.domain.dto.role.RoleRequest;
import br.com.marques.kontaktapi.domain.entity.Role;

public interface RoleService {
    void create(RoleRequest request);
    boolean existsByName(String name);
    Role findByName(String name);
}
