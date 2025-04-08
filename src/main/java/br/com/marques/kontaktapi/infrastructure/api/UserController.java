package br.com.marques.kontaktapi.infrastructure.api;

import br.com.marques.kontaktapi.domain.dto.user.RegisterRequest;
import br.com.marques.kontaktapi.domain.entity.User;
import br.com.marques.kontaktapi.application.usecase.UserCrudUsecase;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Tag(name = "User", description = "User management")
public class UserController {
    private final UserCrudUsecase<User, RegisterRequest> service;

    @Operation(summary = "Create a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    @RateLimiter(name = "RateLimiter", fallbackMethod = "rateLimiterFallback")
    public ResponseEntity<Void> create(@Valid @RequestBody RegisterRequest dto) {
        service.create(dto);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Void> rateLimiterFallback(RegisterRequest dto, Throwable t) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

    @Operation(summary = "Get all users (Staff Exclusive)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users found"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters supplied"),
            @ApiResponse(responseCode = "404", description = "Users not found")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @RateLimiter(name = "RateLimiter", fallbackMethod = "rateLimiterFallback")
    public ResponseEntity<List<User>> list() {
        List<User> users = service.list();
        return ResponseEntity.ok(users);
    }

    public String rateLimiterFallback(Throwable t) {
        return "Too many requests - please try again later.";
    }
}
