package br.com.marques.kontaktapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class KontaktApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(KontaktApiApplication.class, args);
	}

}
