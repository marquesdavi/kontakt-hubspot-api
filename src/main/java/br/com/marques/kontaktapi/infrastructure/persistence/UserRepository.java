package br.com.marques.kontaktapi.infrastructure.persistence;

import br.com.marques.kontaktapi.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAll();
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
