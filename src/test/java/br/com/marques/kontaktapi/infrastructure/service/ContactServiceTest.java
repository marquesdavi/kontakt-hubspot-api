package br.com.marques.kontaktapi.infrastructure.service;

import br.com.marques.kontaktapi.application.usecase.HubspotTokenUsecase;
import br.com.marques.kontaktapi.application.usecase.UserCrudUsecase;
import br.com.marques.kontaktapi.domain.dto.contact.ContactRequest;
import br.com.marques.kontaktapi.domain.entity.User;
import br.com.marques.kontaktapi.infrastructure.data.service.ContactService;
import br.com.marques.kontaktapi.infrastructure.external.HubspotApiHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContactService Tests")
class ContactServiceTest {

    @Mock
    private UserCrudUsecase<User, ?> userCrudUsecase;
    @Mock
    private HubspotTokenUsecase<?, ?> hubspotTokenUsecase;
    @Mock
    private HubspotApiHelper hubspotApiHelper;
    @InjectMocks
    private ContactService contactService;

    private User mockUser;
    private ContactRequest mockContactRequest;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);

        mockContactRequest = new ContactRequest("test@example.com", "John", "Doe");
    }

    @Nested
    @DisplayName("createContact")
    class CreateContactCreationEventEntityTests {

        @Test
        @DisplayName("shouldCreateContact_WhenAccessTokenIsValid")
        void shouldCreateContact_WhenAccessTokenIsValid() {
            String accessToken = "testAccessToken";
            ContactRequest createdContactRequest = new ContactRequest("test@example.com", "John", "Doe");

            when(userCrudUsecase.getLogged()).thenReturn(mockUser);
            when(hubspotTokenUsecase.getAccessTokenByUserId(anyLong())).thenReturn(accessToken);
            when(hubspotApiHelper.executePostJsonCall(anyString(), anyMap(), any(), anyString()))
                    .thenReturn(Mono.just(createdContactRequest));

            contactService.createContact(mockContactRequest);

            verify(userCrudUsecase).getLogged();
            verify(hubspotTokenUsecase).getAccessTokenByUserId(1L);
            verify(hubspotApiHelper).executePostJsonCall("/crm/v3/objects/contacts", Map.of("properties", Map.of("email", "test@example.com", "firstname", "John", "lastname", "Doe")), ContactRequest.class, accessToken);
        }

        @Test
        @DisplayName("shouldThrowIllegalStateException_WhenAccessTokenIsNull")
        void shouldThrowIllegalStateException_WhenAccessTokenIsNull() {
            when(userCrudUsecase.getLogged()).thenReturn(mockUser);
            when(hubspotTokenUsecase.getAccessTokenByUserId(anyLong())).thenReturn(null);

            assertThrows(IllegalStateException.class, () -> contactService.createContact(mockContactRequest));

            verify(userCrudUsecase).getLogged();
            verify(hubspotTokenUsecase).getAccessTokenByUserId(1L);
        }

        @Test
        @DisplayName("shouldThrowIllegalStateException_WhenAccessTokenIsEmpty")
        void shouldThrowIllegalStateException_WhenAccessTokenIsEmpty() {
            when(userCrudUsecase.getLogged()).thenReturn(mockUser);
            when(hubspotTokenUsecase.getAccessTokenByUserId(anyLong())).thenReturn("");

            assertThrows(IllegalStateException.class, () -> contactService.createContact(mockContactRequest));

            verify(userCrudUsecase).getLogged();
            verify(hubspotTokenUsecase).getAccessTokenByUserId(1L);
        }

        @Test
        @DisplayName("shouldHandleApiCallFailure_WhenApiCallFails")
        void shouldHandleApiCallFailure_WhenApiCallFails() {
            String accessToken = "testAccessToken";
            when(userCrudUsecase.getLogged()).thenReturn(mockUser);
            when(hubspotTokenUsecase.getAccessTokenByUserId(anyLong())).thenReturn(accessToken);
            when(hubspotApiHelper.executePostJsonCall(anyString(), anyMap(), any(), anyString()))
                    .thenReturn(Mono.error(new RuntimeException("API call failed")));

            StepVerifier.create(Mono.fromRunnable(() -> contactService.createContact(mockContactRequest)))
                    .expectError(RuntimeException.class)
                    .verify();

            verify(userCrudUsecase).getLogged();
            verify(hubspotTokenUsecase).getAccessTokenByUserId(1L);
            verify(hubspotApiHelper).executePostJsonCall("/crm/v3/objects/contacts", Map.of("properties", Map.of("email", "test@example.com", "firstname", "John", "lastname", "Doe")), ContactRequest.class, accessToken);
        }
    }

    @Nested
    @DisplayName("buildRequestBody")
    class BuildRequestBodyTests {
        @Test
        @DisplayName("shouldBuildRequestBody_WhenContactDTOIsValid")
        void shouldBuildRequestBody_WhenContactDTOIsValid() {
            Map<String, Object> result = contactService.buildRequestBody(mockContactRequest);

            Map<String, String> expectedProperties = Map.of(
                    "email", "test@example.com",
                    "firstname", "John",
                    "lastname", "Doe"
            );

            assertEquals(Map.of("properties", expectedProperties), result);
        }
    }
}