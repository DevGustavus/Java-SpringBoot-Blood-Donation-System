package com.gustavo.blood_donation_system.admin.presentation;

import com.gustavo.blood_donation_system.user.domain.User;
import com.gustavo.blood_donation_system.user.domain.UserRole;

import java.time.Instant;

public record UserSummaryResponse(
        Long id,
        String email,
        UserRole role,
        Instant createdAt) {

    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(user.getId(), user.getEmail(), user.getRole(), user.getCreatedAt());
    }
}
