package br.com.marques.kontaktapi.infra.external;

import br.com.marques.kontaktapi.app.usecase.HubspotTokenUsecase;
import br.com.marques.kontaktapi.domain.dto.hubspot.OAuthCallbackDTO;
import br.com.marques.kontaktapi.domain.dto.hubspot.OAuthTokenResponseDTO;
import br.com.marques.kontaktapi.domain.dto.user.RegisterRequest;
import br.com.marques.kontaktapi.domain.entity.User;
import br.com.marques.kontaktapi.app.strategy.CacheServiceStrategy;
import br.com.marques.kontaktapi.app.usecase.UserCrudUsecase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubspotOAuthService implements HubspotTokenUsecase<OAuthCallbackDTO, OAuthTokenResponseDTO> {

    private final HubspotApiHelper hubspotApiHelper;
    private final CacheServiceStrategy cacheServiceStrategy;
    private final UserCrudUsecase<User, ?> userCrudUsecase;

    @Override
    public String generateAuthorizationUrl() {
        String scopes = "crm.objects.contacts.read crm.objects.contacts.write";
        Long loggedUserId = userCrudUsecase.getLogged().getId();
        String state = loggedUserId + ":" + UUID.randomUUID();
        String encodedState = URLEncoder.encode(state, StandardCharsets.UTF_8);
        return hubspotApiHelper.generateAuthorizationUrl(scopes, encodedState);
    }

    @Override
    public void processTokenExchange(OAuthCallbackDTO callbackDto) {
        Long userId = decodeUserIdFromState(callbackDto.state());

        String code = callbackDto.code();
        MultiValueMap<String, String> params = hubspotApiHelper.buildCallParameters("authorization_code");
        params.add("code", code);

        OAuthTokenResponseDTO tokenResponse = hubspotApiHelper.executeCall(
                        "/oauth/v1/token",
                        params,
                        OAuthTokenResponseDTO.class)
                .doOnNext(tr -> log.info("Token received for user {}", userId))
                .doOnError(error -> log.error("Error exchanging code for token for user {}: {}", userId, error.getMessage()))
                .block();

        persistTokens(tokenResponse, userId);
    }

    @Override
    public OAuthTokenResponseDTO refreshTokenSync(String refreshToken, Long userId) {
        MultiValueMap<String, String> params = hubspotApiHelper.buildCallParameters("refresh_token");
        params.add("refresh_token", refreshToken);

        String refreshTokenKey = getRefreshTokenKey(userId);
        cacheServiceStrategy.delete(refreshTokenKey);

        OAuthTokenResponseDTO tokenResponse = hubspotApiHelper.executeCall(
                        "/oauth/v1/token",
                        params,
                        OAuthTokenResponseDTO.class)
                .doOnNext(tr -> log.info("New token received for user {}", userId))
                .doOnError(error -> log.error("Error refreshing token for user {}: {}", userId, error.getMessage()))
                .block();

        persistTokens(tokenResponse, userId);
        return tokenResponse;
    }

    private Long decodeUserIdFromState(String state) {
        if (state == null || state.isEmpty()) {
            throw new IllegalArgumentException("State parameter is missing or empty");
        }
        try {
            String decodedState = URLDecoder.decode(state, StandardCharsets.UTF_8);
            String[] parts = decodedState.split(":");
            return Long.parseLong(parts[0]);
        } catch (Exception e) {
            log.error("Error decoding state parameter: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid state parameter");
        }
    }

    public void persistTokens(OAuthTokenResponseDTO tokenResponse, Long userId) {
        if (tokenResponse == null) return;
        try {
            log.info("Persisting HubSpot tokens for user {}", userId);
            long accessTokenTTL = tokenResponse.expires_in();
            String accessTokenKey = getAccessTokenKey(userId);
            String refreshTokenKey = getRefreshTokenKey(userId);

            cacheServiceStrategy.set(accessTokenKey, tokenResponse.access_token(), Duration.ofSeconds(accessTokenTTL));
            cacheServiceStrategy.set(refreshTokenKey, tokenResponse.refresh_token(), Duration.ofDays(5));
        } catch (Exception e) {
            log.error("Error persisting HubSpot tokens for user {}: {}", userId, e.getMessage());
        }
    }

    public String getAccessTokenByUserId(Long userId) {
        String accessTokenKey = getAccessTokenKey(userId);
        String token = cacheServiceStrategy.get(accessTokenKey);
        if (token == null || token.isEmpty()) {
            token = accessTokenFallback(userId);
        }
        return token;
    }

    private String accessTokenFallback(Long userId) {
        log.info("Access token not found for user {}. Attempting to refresh token.", userId);
        String refreshToken = getRefreshTokenByUserId(userId);
        if (refreshToken != null && !refreshToken.isEmpty()) {
            OAuthTokenResponseDTO refreshed = refreshTokenSync(refreshToken, userId);
            return (refreshed != null) ? refreshed.access_token() : null;
        }
        log.error("No refresh token found for user {}", userId);
        return null;
    }

    public String getRefreshTokenByUserId(Long userId) {
        String refreshTokenKey = getRefreshTokenKey(userId);
        return cacheServiceStrategy.get(refreshTokenKey);
    }

    private String getAccessTokenKey(Long userId) {
        return "hubspot:access_token:" + userId;
    }

    private String getRefreshTokenKey(Long userId) {
        return "hubspot:refresh_token:" + userId;
    }
}







