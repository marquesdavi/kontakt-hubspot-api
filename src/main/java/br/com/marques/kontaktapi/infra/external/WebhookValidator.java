package br.com.marques.kontaktapi.infra.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Slf4j
@Component
public class WebhookValidator {

    @Value("${hubspot.client.secret}")
    private String clientSecret;

    private static final long MAX_ALLOWED_TIMESTAMP_MILLIS = 300_000;
    private final ObjectMapper objectMapper = new ObjectMapper();


    public boolean validate(HttpServletRequest request, String payload) {
        try {
            String signatureHeader = request.getHeader("X-HubSpot-Signature-v3");
            String timestampHeader = request.getHeader("X-HubSpot-Request-Timestamp");

            if (signatureHeader == null || timestampHeader == null) {
                log.error("Missing required headers for validation.");
                return false;
            }

            if (!isTimestampValid(timestampHeader)) {
                log.error("Request timestamp is too old.");
                return false;
            }

            String normalizedPayload = normalizePayload(payload);

            String stringToSign = buildStringToSign(request, normalizedPayload, timestampHeader);
            log.info("String to sign: {}", stringToSign);

            String computedSignature = computeSignature(stringToSign);
            log.info("Computed signature: {}", computedSignature);

            if (!isSignatureValid(computedSignature, signatureHeader)) {
                log.error("Computed signature {} does not match header {}", computedSignature, signatureHeader);
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("Exception during webhook validation: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean isTimestampValid(String timestampHeader) {
        long timestamp = Long.parseLong(timestampHeader);
        long currentTime = System.currentTimeMillis();
        return currentTime - timestamp <= MAX_ALLOWED_TIMESTAMP_MILLIS;
    }

    private String normalizePayload(String payload) {
        try {
            if (payload == null || payload.trim().isEmpty()) {
                return "";
            }
            Object json = objectMapper.readValue(payload, Object.class);
            return objectMapper.writeValueAsString(json);
        } catch (Exception e) {
            log.error("Error normalizing payload: {}", e.getMessage(), e);
            return payload;
        }
    }

    private String buildStringToSign(HttpServletRequest request, String normalizedPayload, String timestampHeader) {
        String method = request.getMethod();
        String host = request.getHeader("host");
        String requestUri = request.getRequestURI();
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isEmpty()) {
            requestUri = requestUri + "?" + queryString;
        }
        String fullUri = "https://" + host + requestUri;
        return method + fullUri + normalizedPayload + timestampHeader;
    }

    private String computeSignature(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(clientSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(rawHmac);
    }

    private boolean isSignatureValid(String computedSignature, String signatureHeader) {
        return MessageDigest.isEqual(
                computedSignature.getBytes(StandardCharsets.UTF_8),
                signatureHeader.getBytes(StandardCharsets.UTF_8)
        );
    }
}
