package br.com.marques.kontaktapi.infra.mapper;

import br.com.marques.kontaktapi.domain.dto.user.RegisterRequest;
import br.com.marques.kontaktapi.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final BCryptPasswordEncoder passwordEncoder;

    public User toEntity(RegisterRequest registerRequest) {
        if (registerRequest == null) {
            return null;
        }

        User user = new User();
        BeanUtils.copyProperties(registerRequest, user);
        user.setPassword(passwordEncoder.encode(registerRequest.password()));
        return user;
    }
}
