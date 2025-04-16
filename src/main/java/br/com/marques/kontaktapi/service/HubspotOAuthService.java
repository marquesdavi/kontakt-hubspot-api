package br.com.marques.kontaktapi.service;

import br.com.marques.kontaktapi.domain.dto.user.LoginRequest;
import br.com.marques.kontaktapi.domain.dto.user.TokenResponse;
import br.com.marques.kontaktapi.config.hubspot.HubspotApiHelper;
import br.com.marques.kontaktapi.service.gateway.AuthenticationServiceGateway;
import br.com.marques.kontaktapi.service.gateway.OAuthServiceGateway;
import br.com.marques.kontaktapi.domain.dto.hubspot.OAuthCallbackRequest;
import br.com.marques.kontaktapi.domain.dto.hubspot.OAuthTokenResponse;
import br.com.marques.kontaktapi.domain.entity.User;
import br.com.marques.kontaktapi.service.gateway.CacheServiceGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubspotOAuthService implements OAuthServiceGateway<OAuthCallbackRequest, OAuthTokenResponse> {
    private final HubspotApiHelper hubspotApiHelper;
    private final CacheServiceGateway cacheServiceGateway;
    private final AuthenticationServiceGateway<User, LoginRequest, TokenResponse> authenticationService;

    @Override
    public String generateAuthorizationUrl() {
        String scopes = "crm.objects.contacts.read crm.objects.contacts.write";

        Long loggedUserId = authenticationService.getAuthenticated().getId();
        String state = loggedUserId + ":" + UUID.randomUUID();
        String encodedState = URLEncoder.encode(state, StandardCharsets.UTF_8);

        return hubspotApiHelper.generateAuthorizationUrl(scopes, encodedState);
    }

    @Override
    public void processTokenExchange(OAuthCallbackRequest callbackDto) {
        Long userId = decodeUserIdFromState(callbackDto.state());

        String code = callbackDto.code();
        MultiValueMap<String, String> params = hubspotApiHelper.buildCallParameters("authorization_code");
        params.add("code", code);

        OAuthTokenResponse tokenResponse = hubspotApiHelper.executeCall(
                        "/oauth/v1/token",
                        params,
                        OAuthTokenResponse.class)
                .doOnNext(tr -> log.info("Token received for user {}", userId))
                .doOnError(error -> log.error("Error exchanging code for token for user {}: {}", userId, error.getMessage()))
                .block();

        persistTokens(tokenResponse, userId);
    }

    @Override
    public OAuthTokenResponse refreshTokenSync(String refreshToken, Long userId) {
        MultiValueMap<String, String> params = hubspotApiHelper.buildCallParameters("refresh_token");
        params.add("refresh_token", refreshToken);

        String refreshTokenKey = getRefreshTokenKey(userId);
        cacheServiceGateway.delete(refreshTokenKey);

        OAuthTokenResponse tokenResponse = hubspotApiHelper.executeCall(
                        "/oauth/v1/token",
                        params,
                        OAuthTokenResponse.class)
                .doOnNext(tr -> log.info("New token received for user {}", userId))
                .doOnError(error -> log.error("Error refreshing token for user {}: {}", userId, error.getMessage()))
                .block();

        persistTokens(tokenResponse, userId);
        return tokenResponse;
    }

    public Long decodeUserIdFromState(String state) {
        if (Objects.isNull(state) || state.isEmpty())
            throw new IllegalArgumentException("State parameter is missing or empty");

        try {
            String decodedState = URLDecoder.decode(state, StandardCharsets.UTF_8);
            String[] parts = decodedState.split(":");
            return Long.parseLong(parts[0]);
        } catch (Exception e) {
            log.error("Error decoding state parameter: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid state parameter");
        }
    }

    public void persistTokens(OAuthTokenResponse tokenResponse, Long userId) {
        if (Objects.isNull(tokenResponse)) return;

        try {
            log.info("Persisting HubSpot tokens for user {}", userId);
            long accessTokenTTL = tokenResponse.expires_in();
            String accessTokenKey = getAccessTokenKey(userId);
            String refreshTokenKey = getRefreshTokenKey(userId);

            cacheServiceGateway.set(accessTokenKey, tokenResponse.access_token(), Duration.ofSeconds(accessTokenTTL));
            cacheServiceGateway.set(refreshTokenKey, tokenResponse.refresh_token(), Duration.ofDays(5));
        } catch (Exception e) {
            log.error("Error persisting HubSpot tokens for user {}: {}", userId, e.getMessage());
        }
    }

    @Override
    public String getAccessTokenByUserId(Long userId) {
        String accessTokenKey = getAccessTokenKey(userId);
        String token = cacheServiceGateway.get(accessTokenKey);
        if (Objects.isNull(token) || token.isEmpty())
            token = accessTokenFallback(userId);

        return token;
    }

    String accessTokenFallback(Long userId) {
        log.info("Access token not found for user {}. Attempting to refresh token.", userId);
        String refreshToken = getRefreshTokenByUserId(userId);
        if (Objects.nonNull(refreshToken) && !refreshToken.isEmpty()) {
            OAuthTokenResponse refreshed = refreshTokenSync(refreshToken, userId);
            return (Objects.nonNull(refreshed)) ? refreshed.access_token() : null;
        }
        log.error("No refresh token found for user {}", userId);
        return null;
    }

    public String getRefreshTokenByUserId(Long userId) {
        String refreshTokenKey = getRefreshTokenKey(userId);
        return cacheServiceGateway.get(refreshTokenKey);
    }

    String getAccessTokenKey(Long userId) {
        return "hubspot:access_token:" + userId;
    }

    String getRefreshTokenKey(Long userId) {
        return "hubspot:refresh_token:" + userId;
    }
}








