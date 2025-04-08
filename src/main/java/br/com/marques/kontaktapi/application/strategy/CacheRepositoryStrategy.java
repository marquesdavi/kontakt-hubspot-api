package br.com.marques.kontaktapi.application.strategy;

import java.time.Duration;

public interface CacheRepositoryStrategy {
    void set(String key, String value, Duration ttl);
    String get(String key);
     void delete(String key);
}

