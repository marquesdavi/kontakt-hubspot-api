package br.com.marques.kontaktapi.service.impl;

import br.com.marques.kontaktapi.domain.dto.role.RoleRequest;
import br.com.marques.kontaktapi.domain.entity.Role;
import br.com.marques.kontaktapi.domain.repository.IRoleRepository;
import br.com.marques.kontaktapi.exception.AlreadyExistsException;
import br.com.marques.kontaktapi.exception.NotFoundException;
import br.com.marques.kontaktapi.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final IRoleRepository roleRepository;

    @Override
    public void create(RoleRequest request) {
        log.info("Creating role {}", request.name());

        if (existsByName(request.name())) return;

        Role role = new Role();
        BeanUtils.copyProperties(request, role);
        roleRepository.save(role);

        log.info("Role {} created successfully", request.name());
    }

    @Override
    public boolean existsByName(String name) {
        boolean exists = roleRepository.existsByName(name);
        if (exists)
            log.warn("Role with name {} already exists!", name);

        return exists;
    }

    @Override
    public Role findByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> {
                    log.error("Role with name {} not found!", name);
                    return new NotFoundException("Role not found");
                });
    }
}
