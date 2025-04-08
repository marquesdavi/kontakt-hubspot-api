package br.com.marques.kontaktapi.domain.entity;

import br.com.marques.kontaktapi.domain.dto.contact.ContactCreationEventRequest;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "contact_creation_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactCreationEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long eventId;
    private Long subscriptionId;
    private Long portalId;
    private Long appId;
    private LocalDateTime occurredAt;
    private String subscriptionType;
    private Integer attemptNumber;
    private Long objectId;
    private String changeFlag;
    private String changeSource;

    public static ContactCreationEventEntity fromRequest(ContactCreationEventRequest request) {
        return ContactCreationEventEntity.builder()
                .eventId(request.eventId())
                .subscriptionId(request.subscriptionId())
                .portalId(request.portalId())
                .appId(request.appId())
                .occurredAt(convertLongToLocalDateTime(request.occurredAt()))
                .subscriptionType(request.subscriptionType())
                .attemptNumber(request.attemptNumber())
                .objectId(request.objectId())
                .changeFlag(request.changeFlag())
                .changeSource(request.changeSource())
                .build();
    }

    private static LocalDateTime convertLongToLocalDateTime(Long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }
}
