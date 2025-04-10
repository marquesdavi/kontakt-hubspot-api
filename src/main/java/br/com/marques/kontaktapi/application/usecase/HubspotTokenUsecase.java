package br.com.marques.kontaktapi.application.usecase;

public interface HubspotTokenUsecase<Req, Res> {
    String generateAuthorizationUrl();
    void processTokenExchange(Req callback);
    Res refreshTokenSync(String refreshToken,  Long userId);
    String getAccessTokenByUserId(Long userId);
}
