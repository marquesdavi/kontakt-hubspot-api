package br.com.marques.kontaktapi.infra.external;

import br.com.marques.kontaktapi.app.strategy.CacheServiceStrategy;
import br.com.marques.kontaktapi.app.usecase.UserCrudUsecase;
import br.com.marques.kontaktapi.domain.dto.hubspot.OAuthCallbackRequest;
import br.com.marques.kontaktapi.domain.dto.hubspot.OAuthTokenResponse;
import br.com.marques.kontaktapi.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HubspotOAuthService Tests")
class HubspotOAuthServiceTest {

    @Mock
    private HubspotApiHelper hubspotApiHelper;
    @Mock
    private CacheServiceStrategy cacheServiceStrategy;
    @Mock
    private UserCrudUsecase<User, ?> userCrudUsecase;
    @InjectMocks
    private HubspotOAuthService hubspotOAuthService;

    private User loggedUser;

    @BeforeEach
    void setUp() {
        loggedUser = new User();
        loggedUser.setId(1L);
    }

    @Nested
    @DisplayName("generateAuthorizationUrl")
    class GenerateAuthorizationUrlTests {

        @Test
        @DisplayName("shouldGenerateAuthorizationUrl_WhenUserIsLogged")
        void shouldGenerateAuthorizationUrl_WhenUserIsLogged() {
            when(userCrudUsecase.getLogged()).thenReturn(loggedUser);
            String expectedUrl = "https://example.com/auth?scopes=crm.objects.contacts.read%20crm.objects.contacts.write&state=1%3A" + UUID.randomUUID();
            when(hubspotApiHelper.generateAuthorizationUrl(anyString(), anyString())).thenReturn(expectedUrl);

            String result = hubspotOAuthService.generateAuthorizationUrl();

            assertNotNull(result);
            assertTrue(result.contains("https://example.com/auth?scopes=crm.objects.contacts.read%20crm.objects.contacts.write&state=1%3A"));
            verify(userCrudUsecase, times(1)).getLogged();
            verify(hubspotApiHelper, times(1)).generateAuthorizationUrl(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("processTokenExchange")
    class ProcessTokenExchangeTests {

        @Test
        @DisplayName("shouldProcessTokenExchange_WhenStateIsValid")
        void shouldProcessTokenExchange_WhenStateIsValid() {
            String state = "1:" + UUID.randomUUID();
            OAuthCallbackRequest callbackDTO = new OAuthCallbackRequest("testCode", state);
            OAuthTokenResponse tokenResponseDTO = new OAuthTokenResponse(
                    "accessToken",
                    "refreshToken",
                    "Bearer",
                    3600,
                    "crm.objects.contacts.read crm.objects.contacts.write"
            );
            when(hubspotApiHelper.buildCallParameters(anyString())).thenReturn(new LinkedMultiValueMap<>());
            when(hubspotApiHelper.executeCall(anyString(), any(MultiValueMap.class), eq(OAuthTokenResponse.class)))
                    .thenReturn(Mono.just(tokenResponseDTO));

            hubspotOAuthService.processTokenExchange(callbackDTO);

            verify(hubspotApiHelper, times(1)).buildCallParameters("authorization_code");
            verify(hubspotApiHelper, times(1))
                    .executeCall(anyString(), any(MultiValueMap.class), eq(OAuthTokenResponse.class));
            verify(cacheServiceStrategy, times(1))
                    .set(eq("hubspot:access_token:1"), eq("accessToken"), eq(Duration.ofSeconds(3600)));
            verify(cacheServiceStrategy, times(1))
                    .set(eq("hubspot:refresh_token:1"), eq("refreshToken"), eq(Duration.ofDays(5)));
        }

        @Test
        @DisplayName("shouldThrowException_WhenStateIsInvalid")
        void shouldThrowException_WhenStateIsInvalid() {
            OAuthCallbackRequest callbackDTO = new OAuthCallbackRequest("testCode", "invalidState");
            assertThrows(IllegalArgumentException.class, () -> hubspotOAuthService.processTokenExchange(callbackDTO));
        }
    }

    @Nested
    @DisplayName("refreshTokenSync")
    class RefreshTokenSyncTests {

        @Test
        @DisplayName("shouldRefreshTokenSync_WhenCalled")
        void shouldRefreshTokenSync_WhenCalled() {
            String refreshToken = "testRefreshToken";
            Long userId = 1L;
            OAuthTokenResponse tokenResponseDTO = new OAuthTokenResponse(
                    "newAccessToken",
                    "newRefreshToken",
                    "Bearer",
                    3600,
                    "crm.objects.contacts.read crm.objects.contacts.write"
            );
            when(hubspotApiHelper.buildCallParameters(anyString())).thenReturn(new LinkedMultiValueMap<>());
            when(hubspotApiHelper.executeCall(anyString(), any(MultiValueMap.class), eq(OAuthTokenResponse.class)))
                    .thenReturn(Mono.just(tokenResponseDTO));

            OAuthTokenResponse result = hubspotOAuthService.refreshTokenSync(refreshToken, userId);

            assertNotNull(result);
            assertEquals("newAccessToken", result.access_token());
            assertEquals("newRefreshToken", result.refresh_token());
            verify(hubspotApiHelper, times(1)).buildCallParameters("refresh_token");
            verify(hubspotApiHelper, times(1))
                    .executeCall(anyString(), any(MultiValueMap.class), eq(OAuthTokenResponse.class));
            verify(cacheServiceStrategy, times(1)).delete("hubspot:refresh_token:1");
            verify(cacheServiceStrategy, times(1))
                    .set(eq("hubspot:access_token:1"), eq("newAccessToken"), eq(Duration.ofSeconds(3600)));
            verify(cacheServiceStrategy, times(1))
                    .set(eq("hubspot:refresh_token:1"), eq("newRefreshToken"), eq(Duration.ofDays(5)));
        }
    }

    @Nested
    @DisplayName("decodeUserIdFromState")
    class DecodeUserIdFromStateTests {

        @Test
        @DisplayName("shouldDecodeUserId_WhenStateIsValid")
        void shouldDecodeUserId_WhenStateIsValid() {
            String state = "1:" + UUID.randomUUID();
            Long userId = hubspotOAuthService.decodeUserIdFromState(state);
            assertEquals(1L, userId);
        }

        @Test
        @DisplayName("shouldThrowException_WhenStateIsEmpty")
        void shouldThrowException_WhenStateIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> hubspotOAuthService.decodeUserIdFromState(""));
        }

        @Test
        @DisplayName("shouldThrowException_WhenStateIsNull")
        void shouldThrowException_WhenStateIsNull() {
            assertThrows(IllegalArgumentException.class, () -> hubspotOAuthService.decodeUserIdFromState(null));
        }

        @Test
        @DisplayName("shouldThrowException_WhenStateIsInvalid")
        void shouldThrowException_WhenStateIsInvalid() {
            assertThrows(IllegalArgumentException.class, () -> hubspotOAuthService.decodeUserIdFromState("invalid"));
        }
    }

    @Nested
    @DisplayName("persistTokens")
    class PersistTokensTests {

        @Test
        @DisplayName("shouldPersistTokens_WhenTokenResponseIsValid")
        void shouldPersistTokens_WhenTokenResponseIsValid() {
            OAuthTokenResponse tokenResponseDTO = new OAuthTokenResponse(
                    "accessToken",
                    "refreshToken",
                    "Bearer",
                    3600,
                    "crm.objects.contacts.read crm.objects.contacts.write"
            );
            hubspotOAuthService.persistTokens(tokenResponseDTO, 1L);

            verify(cacheServiceStrategy, times(1))
                    .set("hubspot:access_token:1", "accessToken", Duration.ofSeconds(3600));
            verify(cacheServiceStrategy, times(1))
                    .set("hubspot:refresh_token:1", "refreshToken", Duration.ofDays(5));
        }

        @Test
        @DisplayName("shouldNotPersistTokens_WhenTokenResponseIsNull")
        void shouldNotPersistTokens_WhenTokenResponseIsNull() {
            hubspotOAuthService.persistTokens(null, 1L);
            verifyNoInteractions(cacheServiceStrategy);
        }
    }

    @Nested
    @DisplayName("getAccessTokenByUserId")
    class GetAccessTokenByUserIdTests {

        @Test
        @DisplayName("shouldReturnAccessToken_WhenCachedTokenExists")
        void shouldReturnAccessToken_WhenCachedTokenExists() {
            when(cacheServiceStrategy.get("hubspot:access_token:1")).thenReturn("cachedAccessToken");
            String token = hubspotOAuthService.getAccessTokenByUserId(1L);
            assertEquals("cachedAccessToken", token);
        }

        @Test
        @DisplayName("shouldRefreshAndReturnAccessToken_WhenCachedTokenMissingAndRefreshTokenExists")
        void shouldRefreshAndReturnAccessToken_WhenCachedTokenMissingAndRefreshTokenExists() {
            when(cacheServiceStrategy.get("hubspot:access_token:1")).thenReturn(null);
            when(cacheServiceStrategy.get("hubspot:refresh_token:1")).thenReturn("testRefreshToken");
            OAuthTokenResponse tokenResponseDTO = new OAuthTokenResponse(
                    "newAccessToken",
                    "newRefreshToken",
                    "Bearer",
                    3600,
                    "crm.objects.contacts.read crm.objects.contacts.write"
            );
            when(hubspotApiHelper.buildCallParameters(anyString())).thenReturn(new LinkedMultiValueMap<>());
            when(hubspotApiHelper.executeCall(anyString(), any(MultiValueMap.class), eq(OAuthTokenResponse.class)))
                    .thenReturn(Mono.just(tokenResponseDTO));

            String token = hubspotOAuthService.getAccessTokenByUserId(1L);
            assertEquals("newAccessToken", token);
            verify(cacheServiceStrategy, times(1)).get("hubspot:access_token:1");
            verify(cacheServiceStrategy, times(1)).get("hubspot:refresh_token:1");
            verify(hubspotApiHelper, times(1))
                    .executeCall(anyString(), any(MultiValueMap.class), eq(OAuthTokenResponse.class));
        }

        @Test
        @DisplayName("shouldReturnNull_WhenNoAccessOrRefreshTokenFound")
        void shouldReturnNull_WhenNoAccessOrRefreshTokenFound() {
            when(cacheServiceStrategy.get("hubspot:access_token:1")).thenReturn(null);
            when(cacheServiceStrategy.get("hubspot:refresh_token:1")).thenReturn(null);

            String token = hubspotOAuthService.getAccessTokenByUserId(1L);
            assertNull(token);
            verify(cacheServiceStrategy, times(1)).get("hubspot:access_token:1");
            verify(cacheServiceStrategy, times(1)).get("hubspot:refresh_token:1");
            verify(hubspotApiHelper, never())
                    .executeCall(anyString(), any(MultiValueMap.class), eq(OAuthTokenResponse.class));
        }
    }

    @Nested
    @DisplayName("getRefreshTokenByUserId")
    class GetRefreshTokenByUserIdTests {

        @Test
        @DisplayName("shouldReturnRefreshToken_WhenTokenExists")
        void shouldReturnRefreshToken_WhenTokenExists() {
            when(cacheServiceStrategy.get("hubspot:refresh_token:1")).thenReturn("testRefreshToken");
            String token = hubspotOAuthService.getRefreshTokenByUserId(1L);
            assertEquals("testRefreshToken", token);
            verify(cacheServiceStrategy, times(1)).get("hubspot:refresh_token:1");
        }
    }
}
