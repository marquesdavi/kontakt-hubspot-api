package br.com.marques.kontaktapi.infra.persistence;

import br.com.marques.kontaktapi.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {
    List<User> findAll();
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
