package com.gustavo.blood_donation_system.authentication.infrastructure;

import com.gustavo.blood_donation_system.user.domain.User;
import com.gustavo.blood_donation_system.user.domain.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-with-more-than-32-bytes-length-ok";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        SecretKey key = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtEncoder encoder = NimbusJwtEncoder.withSecretKey(key)
                .algorithm(MacAlgorithm.HS256)
                .build();
        jwtService = new JwtService(encoder, 3600);
    }

    @Test
    void shouldGenerateTokenWithExpectedClaims() {
        User user = User.create("joao@example.com", "hashed-password", UserRole.USER);

        String token = jwtService.generateToken(user);

        Jwt decoded = NimbusJwtDecoder.withSecretKey(
                        new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"))
                .macAlgorithm(MacAlgorithm.HS256)
                .build()
                .decode(token);

        assertThat(decoded.getSubject()).isEqualTo("joao@example.com");
        assertThat(decoded.getClaimAsString("role")).isEqualTo("USER");
        assertThat(decoded.getIssuer().toString()).isEqualTo("https://blood-donation-system");
        assertThat(decoded.getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    void shouldExposeConfiguredExpiration() {
        assertThat(jwtService.getExpirationSeconds()).isEqualTo(3600);
    }
}
