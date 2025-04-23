package br.com.marques.kontaktapi.service.gateway;

import org.springframework.data.domain.Page;

import java.util.List;

public interface WebhookServiceGateway<Entity, Req> {
    void process(List<Req> events);
    Page<Entity> listEvents(Integer page,
                            Integer size);
}
