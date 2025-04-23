package br.com.marques.kontaktapi.service.gateway;

import java.time.Duration;

public interface CacheServiceGateway {
    void set(String key, String value, Duration ttl);
    String get(String key);
     void delete(String key);
}

