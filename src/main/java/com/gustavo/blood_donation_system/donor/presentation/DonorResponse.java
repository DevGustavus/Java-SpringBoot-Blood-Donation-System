package com.gustavo.blood_donation_system.donor.presentation;

import com.gustavo.blood_donation_system.user.domain.BloodType;

public record DonorResponse(
        Long id,
        String fullName,
        BloodType bloodType,
        String city,
        String state,
        String zipCode) {
}
