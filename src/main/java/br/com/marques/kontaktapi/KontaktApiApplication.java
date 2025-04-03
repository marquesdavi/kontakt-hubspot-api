package br.com.marques.kontaktapi;

import br.com.marques.kontaktapi.domain.dto.role.RoleRequest;
import br.com.marques.kontaktapi.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableCaching
@SpringBootApplication
@RequiredArgsConstructor
public class KontaktApiApplication implements CommandLineRunner {
	private final RoleService roleService;

	public static void main(String[] args) {
		SpringApplication.run(KontaktApiApplication.class, args);
	}

	@Override
	public void run(String... args) {
		roleService.create(new RoleRequest("ADMIN", "Administrator role"));
		roleService.create(new RoleRequest("USER", "Default user role"));
	}
}
