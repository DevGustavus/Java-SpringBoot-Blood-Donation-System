package com.gustavo.blood_donation_system.user.presentation;

import com.gustavo.blood_donation_system.user.domain.BloodType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UserProfileResponse(
        Long userId,
        String email,
        String fullName,
        String phone,
        LocalDate birthDate,
        BigDecimal weightKg,
        BigDecimal heightCm,
        BloodType bloodType,
        String address,
        String city,
        String state,
        String zipCode) {
}
