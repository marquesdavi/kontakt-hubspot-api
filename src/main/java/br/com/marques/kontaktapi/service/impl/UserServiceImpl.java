package br.com.marques.kontaktapi.service.impl;

import br.com.marques.kontaktapi.domain.dto.user.RegisterRequest;
import br.com.marques.kontaktapi.domain.entity.Role;
import br.com.marques.kontaktapi.domain.entity.User;
import br.com.marques.kontaktapi.domain.mapper.UserMapper;
import br.com.marques.kontaktapi.domain.repository.IRoleRepository;
import br.com.marques.kontaktapi.domain.repository.IUserRepository;
import br.com.marques.kontaktapi.exception.AlreadyExistsException;
import br.com.marques.kontaktapi.exception.NotFoundException;
import br.com.marques.kontaktapi.service.RoleService;
import br.com.marques.kontaktapi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final IUserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleService roleService;

    @Override
    @Transactional
    public void create(RegisterRequest dto) {
        log.info("Creating new user with email: {}", dto.email());

        Role userRole = roleService.findByName(Role.Values.USER.name());
        existsByEmail(dto.email());

        User user = userMapper.toEntity(dto, Set.of(userRole));
        userRepository.save(user);

        log.info("User with email {} successfully created.", dto.email());
    }

    public void existsByEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            log.warn("User with email {} already exists!", email);
            throw new AlreadyExistsException("User already exists");
        }
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    public List<User> list() {
        return userRepository.findAll();
    }
}
