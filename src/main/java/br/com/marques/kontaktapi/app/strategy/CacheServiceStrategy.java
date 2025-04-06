package br.com.marques.kontaktapi.app.strategy;

import java.time.Duration;

public interface CacheServiceStrategy {
    void set(String key, String value, Duration ttl);
    String get(String key);
     void delete(String key);
}

