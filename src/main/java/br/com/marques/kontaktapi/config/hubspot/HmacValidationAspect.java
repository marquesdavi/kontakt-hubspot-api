package br.com.marques.kontaktapi.config.hubspot;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
@Aspect
@Component
public class HmacValidationAspect {

    @Value("${hubspot.client.secret}")
    private String clientSecret;

    public static final String X_HUB_SPOT_REQUEST_TIMESTAMP = "X-HubSpot-Request-Timestamp";
    public static final String X_HUB_SPOT_SIGNATURE_V_3 = "X-HubSpot-Signature-v3";
    public static final String HTTP_PROTOCOL = "https://";
    public static final String HOST = "host";


    @Around("@annotation(br.com.marques.kontaktapi.config.hubspot.HmacValidation)")
    public Object validateHmac(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("Validating HMAC...");

        HttpServletRequest request = getCurrentHttpRequest();
        if (request == null) return joinPoint.proceed();

        String method = request.getMethod();
        if (!Objects.equals(method, "POST")) return joinPoint.proceed();

        String body = extractRequestBody(request);

        String uri = request.getRequestURI();
        String timestamp = request.getHeader(X_HUB_SPOT_REQUEST_TIMESTAMP);
        String host = HTTP_PROTOCOL + request.getHeader(HOST);
        String signatureBase = method + host + uri + body + timestamp;

        String expectedSignature = generateHmac(signatureBase, clientSecret);
        String receivedSignature = request.getHeader(X_HUB_SPOT_SIGNATURE_V_3);

        if (!Objects.equals(expectedSignature, receivedSignature)) {
            log.error("HMAC validation failed! Expected: {}, Received: {}", expectedSignature, receivedSignature);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Signature");
        }

        log.info("HMAC validation passed!");
        return joinPoint.proceed();
    }

    private HttpServletRequest getCurrentHttpRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) attributes).getRequest();
        }
        return null;
    }

    private String extractRequestBody(HttpServletRequest request) {
        try {
            if (request instanceof ContentCachingRequestWrapper cachingRequest) {
                byte[] content = cachingRequest.getContentAsByteArray();
                if (content.length > 0) {
                    String body = new String(content, StandardCharsets.UTF_8);
                    log.debug("Request body (from cache): {}", body);
                    return body;
                }
            }

            String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            log.debug("Request body (direct read): {}", body);
            return body;
        } catch (Exception e) {
            log.error("Erro ao ler o corpo da requisição", e);
            return "";
        }
    }

    public static String generateHmac(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar HMAC", e);
        }
    }
}