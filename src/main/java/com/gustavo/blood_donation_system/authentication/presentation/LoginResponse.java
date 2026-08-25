package com.gustavo.blood_donation_system.authentication.presentation;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds) {
}
