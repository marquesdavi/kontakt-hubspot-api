package br.com.marques.kontaktapi.infra.config.security;

import br.com.marques.kontaktapi.infra.external.WebhookValidator;
import br.com.marques.kontaktapi.infra.interceptor.HubspotWebhookInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final WebhookValidator webhookValidator;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HubspotWebhookInterceptor(webhookValidator))
                .addPathPatterns("/api/hubspot/webhook/**");
    }
}
