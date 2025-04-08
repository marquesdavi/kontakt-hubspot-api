package br.com.marques.kontaktapi.infrastructure.data.service;

import br.com.marques.kontaktapi.application.usecase.UserCrudUsecase;
import br.com.marques.kontaktapi.domain.dto.user.LoginRequest;
import br.com.marques.kontaktapi.domain.dto.user.RegisterRequest;
import br.com.marques.kontaktapi.domain.dto.user.TokenResponse;
import br.com.marques.kontaktapi.domain.entity.User;
import br.com.marques.kontaktapi.application.usecase.UserAuthenticationUsecase;
import br.com.marques.kontaktapi.infrastructure.config.resilience.Resilient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthenticationService
        implements UserAuthenticationUsecase<User, LoginRequest, TokenResponse> {
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final UserCrudUsecase<User, RegisterRequest> userCrudUsecase;

    @Value("${jwt.token.expires-in:3600}")
    private long expiresIn;
    private static final String ISSUER = "kontakt-api";

    @Override
    @Resilient(rateLimiter = "RateLimiter", circuitBreaker = "CircuitBreaker")
    public TokenResponse authenticate(LoginRequest request) {
        log.info("Tentativa de login para o email: {}", request.email());
        User user = userCrudUsecase.findByEmail(request.email());

        if (Objects.isNull(user) || !isPasswordCorrect(request.password(), user.getPassword())) {
            log.warn("Falha na tentativa de login para o email: {}", request.email());
            throw new BadCredentialsException("Usuário ou senha inválidos!");
        }
        return generateResponse(user);
    }

    private boolean isPasswordCorrect(String requestPassword, String userPassword) {
        return passwordEncoder.matches(requestPassword, userPassword);
    }

    @Override
    public TokenResponse generateResponse(User user) {
        JwtClaimsSet claims = buildJwtClaimsSet(user);
        String jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return new TokenResponse(jwtValue, expiresIn);
    }

    private JwtClaimsSet buildJwtClaimsSet(User user) {
        Instant now = Instant.now();

        return JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiresIn))
                .claim("role", user.getRole().name())
                .claim("scope", user.getRole().name())
                .build();
    }
}
