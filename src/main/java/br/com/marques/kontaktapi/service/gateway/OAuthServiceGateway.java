package br.com.marques.kontaktapi.service.gateway;

public interface OAuthServiceGateway<Req, Res> {
    String generateAuthorizationUrl();
    void processTokenExchange(Req callback);
    Res refreshTokenSync(String refreshToken,  Long userId);
    String getAccessTokenByUserId(Long userId);
}
