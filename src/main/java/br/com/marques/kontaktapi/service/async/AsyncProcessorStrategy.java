package br.com.marques.kontaktapi.service.async;

public interface AsyncProcessorStrategy {
    void process(String payload);
}
