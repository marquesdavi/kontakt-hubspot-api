package br.com.marques.kontaktapi.domain.dto.contact;

import java.io.Serializable;

public record ContactCreationEventRequest(
        Long eventId,
        Long subscriptionId,
        Long portalId,
        Long appId,
        Long occurredAt,
        String subscriptionType,
        Integer attemptNumber,
        Long objectId,
        String changeFlag,
        String changeSource
) implements Serializable {
}