package br.com.marques.kontaktapi.infrastructure.config.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ResilientAspect {

    private final RateLimiterRegistry rateLimiterRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Around("@annotation(resilient)")
    public Object applyResilience(ProceedingJoinPoint joinPoint, Resilient resilient) throws Throwable {
        RateLimiter rateLimiter = resolveRateLimiter(resilient.rateLimiter());
        CircuitBreaker circuitBreaker = resolveCircuitBreaker(resilient.circuitBreaker());
        Supplier<Object> decoratedSupplier = createDecoratedSupplier(joinPoint, rateLimiter, circuitBreaker);

        try {
            return decoratedSupplier.get();
        } catch (Exception e) {
            if (containsRequestNotPermitted(e)) {
                log.warn("Request blocked by RateLimiter in method {}: {}",
                        joinPoint.getSignature().getName(), e.getMessage());
                throw e;
            }
            log.error("Resilience error in method {}: {}", joinPoint.getSignature().getName(), e.getMessage());
            if (!resilient.fallbackMethod().isEmpty()) {
                Object fallbackResult = invokeFallback(joinPoint, resilient.fallbackMethod(), e);
                if (fallbackResult != null) {
                    return fallbackResult;
                } else {
                    throw new RuntimeException("Fallback method returned null.", e);
                }
            } else {
                return getDefaultFallbackValue(joinPoint);
            }
        }
    }

    private boolean containsRequestNotPermitted(Throwable t) {
        if (t instanceof RequestNotPermitted) {
            return true;
        } else if (t.getCause() != null) {
            return containsRequestNotPermitted(t.getCause());
        }
        return false;
    }

    private RateLimiter resolveRateLimiter(String rateLimiterName) {
        return (rateLimiterName != null && !rateLimiterName.isEmpty())
                ? rateLimiterRegistry.rateLimiter(rateLimiterName)
                : null;
    }

    private CircuitBreaker resolveCircuitBreaker(String circuitBreakerName) {
        return (circuitBreakerName != null && !circuitBreakerName.isEmpty())
                ? circuitBreakerRegistry.circuitBreaker(circuitBreakerName)
                : null;
    }

    private Supplier<Object> createDecoratedSupplier(ProceedingJoinPoint joinPoint,
                                                     RateLimiter rateLimiter,
                                                     CircuitBreaker circuitBreaker) {
        Supplier<Object> supplier = () -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        };

        if (circuitBreaker != null) {
            supplier = CircuitBreaker.decorateSupplier(circuitBreaker, supplier);
        }
        if (rateLimiter != null) {
            supplier = RateLimiter.decorateSupplier(rateLimiter, supplier);
        }
        return supplier;
    }

    private Object invokeFallback(ProceedingJoinPoint joinPoint, String fallbackMethodName, Exception exception)
            throws Throwable {
        Object target = joinPoint.getTarget();
        Object[] originalArgs = joinPoint.getArgs();
        Object[] fallbackArgs = buildFallbackArguments(originalArgs, exception);

        Method fallbackMethod = findFallbackMethod(target, fallbackMethodName, fallbackArgs.length);
        if (fallbackMethod != null) {
            try {
                return fallbackMethod.invoke(target, fallbackArgs);
            } catch (InvocationTargetException ite) {
                Throwable targetException = ite.getTargetException();
                if (targetException instanceof RuntimeException) {
                    throw (RuntimeException) targetException;
                } else {
                    throw new RuntimeException(targetException);
                }
            }
        } else {
            throw new IllegalStateException("Fallback method not found: " + fallbackMethodName);
        }
    }

    private Object[] buildFallbackArguments(Object[] originalArgs, Exception exception) {
        Object[] fallbackArgs = new Object[originalArgs.length + 1];
        System.arraycopy(originalArgs, 0, fallbackArgs, 0, originalArgs.length);
        fallbackArgs[fallbackArgs.length - 1] = exception;
        return fallbackArgs;
    }

    private Method findFallbackMethod(Object target, String fallbackMethodName, int fallbackArgsLength) {
        Class<?> targetClass = AopUtils.getTargetClass(target);
        for (Method method : targetClass.getMethods()) {
            if (method.getName().equals(fallbackMethodName)
                    && method.getParameterCount() == fallbackArgsLength) {
                return method;
            }
        }
        for (Method method : targetClass.getDeclaredMethods()) {
            if (method.getName().equals(fallbackMethodName)
                    && method.getParameterCount() == fallbackArgsLength) {
                method.setAccessible(true);
                return method;
            }
        }
        return null;
    }

    private Object getDefaultFallbackValue(ProceedingJoinPoint joinPoint) {
        Class<?> returnType = ((MethodSignature) joinPoint.getSignature()).getReturnType();
        if (returnType.equals(Void.TYPE)) {
            return null;
        } else if (Mono.class.isAssignableFrom(returnType)) {
            return Mono.empty();
        }
        return null;
    }
}
