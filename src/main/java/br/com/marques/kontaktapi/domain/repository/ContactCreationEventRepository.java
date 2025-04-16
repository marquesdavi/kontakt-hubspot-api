package br.com.marques.kontaktapi.domain.repository;

import br.com.marques.kontaktapi.domain.entity.ContactCreationEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactCreationEventRepository extends JpaRepository<ContactCreationEventEntity, Long> {
}
