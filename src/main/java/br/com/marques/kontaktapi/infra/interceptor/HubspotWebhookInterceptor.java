package br.com.marques.kontaktapi.infra.interceptor;

import br.com.marques.kontaktapi.infra.external.WebhookValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.UnsupportedEncodingException;

@Slf4j
@RequiredArgsConstructor
public class HubspotWebhookInterceptor implements HandlerInterceptor {

    private final WebhookValidator webhookValidator;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        ContentCachingRequestWrapper cachingRequest = (request instanceof ContentCachingRequestWrapper)
                ? (ContentCachingRequestWrapper) request
                : new ContentCachingRequestWrapper(request);

        String payload = getPayload(cachingRequest);
        if (!webhookValidator.validate(cachingRequest, payload)) {
            log.error("Webhook signature invalid.");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid webhook signature");
            return false;
        }
        return true;
    }

    private String getPayload(ContentCachingRequestWrapper request) {
        try {
            byte[] buf = request.getContentAsByteArray();
            return new String(buf, request.getCharacterEncoding());
        } catch (UnsupportedEncodingException e) {
            log.error("Error reading request payload: {}", e.getMessage());
            return "";
        }
    }
}
