package br.com.marques.kontaktapi.config.resilience;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Resilient {
    String rateLimiter() default "";
    String circuitBreaker() default "";
    String fallbackMethod() default "";
}
