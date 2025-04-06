package br.com.marques.kontaktapi;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableCaching
@SpringBootApplication
@RequiredArgsConstructor
public class KontaktApiApplication{
	public static void main(String[] args) {
		SpringApplication.run(KontaktApiApplication.class, args);
	}
}
