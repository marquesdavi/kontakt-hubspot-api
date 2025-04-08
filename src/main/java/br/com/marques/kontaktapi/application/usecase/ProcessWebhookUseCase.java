package br.com.marques.kontaktapi.application.usecase;

import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProcessWebhookUseCase<Entity, Req> {
    void process(List<Req> events);
    Page<Entity> listEvents(Integer page,
                            Integer size);
}
