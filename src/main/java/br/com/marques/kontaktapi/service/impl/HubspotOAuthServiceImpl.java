package br.com.marques.kontaktapi.service.impl;

import br.com.marques.kontaktapi.domain.dto.hubspot.OAuthCallbackDTO;
import br.com.marques.kontaktapi.domain.dto.hubspot.OAuthTokenResponseDTO;
import br.com.marques.kontaktapi.service.HubspotOAuthService;
import br.com.marques.kontaktapi.util.HubspotApiHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubspotOAuthServiceImpl implements HubspotOAuthService {

    private final HubspotApiHelper hubspotApiHelper;

    @Override
    public String generateAuthorizationUrl() {
        String scopes = "crm.objects.contacts.read crm.objects.contacts.write";

        return hubspotApiHelper.generateAuthorizationUrl(scopes);
    }

    @Override
    public OAuthTokenResponseDTO processTokenExchange(OAuthCallbackDTO callbackDto) {
        String code = callbackDto.code();
        MultiValueMap<String, String> params = hubspotApiHelper.buildCallParameters("authorization_code");
        params.add("code", code);

        OAuthTokenResponseDTO tokenResponse = hubspotApiHelper.executeCall(params, OAuthTokenResponseDTO.class)
                .block();

        if (Objects.nonNull(tokenResponse))
            log.info("Token recebido: {}", tokenResponse.access_token());

        return tokenResponse;
    }

    @Override
    public void refreshAccessToken(String refreshToken) {
        MultiValueMap<String, String> params = hubspotApiHelper.buildCallParameters("refresh_token");
        params.add("refresh_token", refreshToken);

        hubspotApiHelper.executeCall(params, OAuthTokenResponseDTO.class)
                .subscribe(tokenResponse -> {
                    log.info("Novo token recebido: {}", tokenResponse.access_token());
                    // Atualize o armazenamento do token conforme necessário
                }, error -> {
                    log.error("Erro ao atualizar token: {}", error.getMessage());
                });
    }
}
