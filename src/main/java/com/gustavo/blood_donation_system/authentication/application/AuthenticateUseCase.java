package com.gustavo.blood_donation_system.authentication.application;

import com.gustavo.blood_donation_system.authentication.infrastructure.JwtService;
import com.gustavo.blood_donation_system.shared.UnauthorizedException;
import com.gustavo.blood_donation_system.user.domain.User;
import com.gustavo.blood_donation_system.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticateUseCase {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthenticationResult execute(String email, String rawPassword) {
        String normalizedEmail = normalizeEmail(email);
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, rawPassword));
        } catch (AuthenticationException ex) {
            log.warn("Authentication failed for email {}", normalizedEmail);
            throw new UnauthorizedException("Invalid email or password");
        }
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + normalizedEmail));
        String accessToken = jwtService.generateToken(user);
        return new AuthenticationResult(accessToken, jwtService.getExpirationSeconds());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    public record AuthenticationResult(String accessToken, long expiresInSeconds) {
    }
}
