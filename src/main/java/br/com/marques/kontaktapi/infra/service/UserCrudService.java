package br.com.marques.kontaktapi.infra.service;

import br.com.marques.kontaktapi.app.usecase.UserCrudUsecase;
import br.com.marques.kontaktapi.domain.entity.RoleEnum;
import br.com.marques.kontaktapi.infra.config.security.IAuthenticationFacade;
import br.com.marques.kontaktapi.domain.dto.user.RegisterRequest;
import br.com.marques.kontaktapi.domain.entity.User;
import br.com.marques.kontaktapi.infra.mapper.UserMapper;
import br.com.marques.kontaktapi.infra.persistence.IUserRepository;
import br.com.marques.kontaktapi.infra.exception.AlreadyExistsException;
import br.com.marques.kontaktapi.infra.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCrudService implements UserCrudUsecase<User, RegisterRequest> {
    private final IAuthenticationFacade authenticationFacade;
    private final IUserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void create(RegisterRequest dto) {
        log.info("Creating new user with email: {}", dto.email());
        existsByEmail(dto.email());

        User user = userMapper.toEntity(dto);
        user.setRole(RoleEnum.USER);

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

    public User findByIdOrElseThrow(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("User not found")
        );
    }

    @Override
    public User getLogged(){
        Authentication currentSession = authenticationFacade.getAuthentication();
        Long userId = Long.parseLong(currentSession.getName());
        return findByIdOrElseThrow(userId);
    }
}
