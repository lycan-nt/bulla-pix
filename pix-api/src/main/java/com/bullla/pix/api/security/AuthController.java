package com.bullla.pix.api.security;

import com.bullla.pix.api.dto.TokenRequest;
import com.bullla.pix.api.dto.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Emissão de token JWT para acesso à API")
public class AuthController {

    private final DemoTokenService demoTokenService;

    public AuthController(DemoTokenService demoTokenService) {
        this.demoTokenService = demoTokenService;
    }

    @PostMapping("/token")
    @Operation(
            summary = "Gerar token JWT",
            description = "Emite um token de acesso."
    )
    public TokenResponse token(@Valid @RequestBody TokenRequest request) {
        String token = demoTokenService.issueToken(request.username(), request.password());
        return new TokenResponse(token, "Bearer");
    }

    @ExceptionHandler(InvalidDemoCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleInvalidCredentials(InvalidDemoCredentialsException ex) {
        return Map.of("error", ex.getMessage());
    }
}
