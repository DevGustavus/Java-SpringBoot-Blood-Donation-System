package com.gustavo.blood_donation_system.authentication.presentation;

import com.gustavo.blood_donation_system.authentication.application.AuthenticateUseCase;
import com.gustavo.blood_donation_system.authentication.application.RegisterUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final AuthenticateUseCase authenticateUseCase;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        RegisterUseCase.RegisteredUser registered =
                registerUseCase.execute(request.name(), request.email(), request.password());
        return new RegisterResponse(registered.id(), registered.name(), registered.email());
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        AuthenticateUseCase.AuthenticationResult result =
                authenticateUseCase.execute(request.email(), request.password());
        return new LoginResponse(result.accessToken(), "Bearer", result.expiresInSeconds());
    }
}
