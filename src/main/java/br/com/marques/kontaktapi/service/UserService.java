package br.com.marques.kontaktapi.service;

import br.com.marques.kontaktapi.service.gateway.UserServiceGateway;
import br.com.marques.kontaktapi.domain.entity.RoleEnum;
import br.com.marques.kontaktapi.config.resilience.Resilient;
import br.com.marques.kontaktapi.domain.dto.user.RegisterRequest;
import br.com.marques.kontaktapi.domain.entity.User;
import br.com.marques.kontaktapi.domain.repository.UserRepository;
import br.com.marques.kontaktapi.controller.exception.AlreadyExistsException;
import br.com.marques.kontaktapi.controller.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserServiceGateway<User, RegisterRequest> {
    private final UserRepository userRepository;

    @Override
    @Transactional
    @Resilient(rateLimiter = "RateLimiter", circuitBreaker = "CircuitBreaker")
    public void create(RegisterRequest dto) {
        log.info("Creating new user with email: {}", dto.email());
        existsByEmail(dto.email());

        User user = User.fromRequest(dto);
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
    @Resilient(rateLimiter = "RateLimiter", circuitBreaker = "CircuitBreaker")
    public List<User> list() {
        return userRepository.findAll();
    }

    @Override
    public User findByIdOrElseThrow(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("User not found")
        );
    }
}
