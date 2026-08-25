package com.gustavo.blood_donation_system.donor.presentation;

import jakarta.validation.constraints.NotNull;

public record UpdateAvailabilityRequest(
        @NotNull Boolean available) {
}
