package br.com.marques.kontaktapi.service.impl;

import br.com.marques.kontaktapi.domain.entity.Role;
import br.com.marques.kontaktapi.domain.repository.IRoleRepository;
import br.com.marques.kontaktapi.exception.NotFoundException;
import br.com.marques.kontaktapi.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final IRoleRepository roleRepository;

    @Override
    public Role findByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> {
                    log.error("Role with name {} not found!", name);
                    return new NotFoundException("Role not found");
                });
    }
}
