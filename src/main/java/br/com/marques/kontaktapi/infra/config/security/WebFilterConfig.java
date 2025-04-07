package br.com.marques.kontaktapi.infra.config.security;

import br.com.marques.kontaktapi.infra.filter.RequestCachingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebFilterConfig {

    @Bean
    public FilterRegistrationBean<RequestCachingFilter> cachingFilter() {
        FilterRegistrationBean<RequestCachingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestCachingFilter());
        registrationBean.addUrlPatterns("/api/hubspot/webhook/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
