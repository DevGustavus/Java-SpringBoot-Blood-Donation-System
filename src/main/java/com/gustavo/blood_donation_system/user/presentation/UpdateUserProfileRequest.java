package com.gustavo.blood_donation_system.user.presentation;

import com.gustavo.blood_donation_system.user.domain.BloodType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateUserProfileRequest(
        @NotBlank @Size(max = 150) String fullName,
        @Size(max = 20) String phone,
        @NotNull @Past LocalDate birthDate,
        @NotNull @DecimalMin("30.0") @DecimalMax("300.0") BigDecimal weightKg,
        @NotNull @DecimalMin("100.0") @DecimalMax("250.0") BigDecimal heightCm,
        @NotNull BloodType bloodType,
        @Size(max = 255) String address,
        @Size(max = 100) String city,
        @Size(min = 2, max = 2) String state,
        @Size(max = 9) String zipCode) {
}
