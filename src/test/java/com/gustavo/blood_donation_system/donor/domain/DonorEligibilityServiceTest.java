package com.gustavo.blood_donation_system.donor.domain;

import com.gustavo.blood_donation_system.user.domain.User;
import com.gustavo.blood_donation_system.user.domain.UserProfile;
import com.gustavo.blood_donation_system.user.domain.UserRole;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DonorEligibilityServiceTest {

    private final DonorEligibilityService eligibilityService = new DonorEligibilityService();

    private UserProfile profile(LocalDate birthDate, String weightKg) {
        User user = User.create("donor@example.com", "hash", UserRole.USER);
        UserProfile profile = UserProfile.create(user, "Donor");
        profile.update("Donor", null, birthDate, weightKg == null ? null : new BigDecimal(weightKg),
                null, null, null, null, null, null);
        return profile;
    }

    @Test
    void shouldBeEligibleWithValidAgeAndWeight() {
        UserProfile profile = profile(LocalDate.now().minusYears(30), "75.5");

        assertThat(eligibilityService.isEligible(profile)).isTrue();
        assertThat(eligibilityService.evaluate(profile)).isEmpty();
    }

    @Test
    void shouldBeEligibleAtMinimumAgeBoundary() {
        UserProfile profile = profile(LocalDate.now().minusYears(18), "50");

        assertThat(eligibilityService.isEligible(profile)).isTrue();
    }

    @Test
    void shouldBeIneligibleBelowMinimumAge() {
        UserProfile profile = profile(LocalDate.now().minusYears(18).plusDays(1), "75");

        assertThat(eligibilityService.evaluate(profile))
                .contains("Donor must be at least 18 years old");
    }

    @Test
    void shouldBeIneligibleAboveMaximumAge() {
        UserProfile profile = profile(LocalDate.now().minusYears(70), "75");

        assertThat(eligibilityService.evaluate(profile))
                .contains("Donor must be at most 69 years old");
    }

    @Test
    void shouldBeIneligibleBelowMinimumWeight() {
        UserProfile profile = profile(LocalDate.now().minusYears(30), "49.9");

        assertThat(eligibilityService.evaluate(profile))
                .contains("Donor must weigh at least 50 kg");
    }

    @Test
    void shouldBeIneligibleWhenRequiredDataIsMissing() {
        UserProfile profile = profile(null, null);

        assertThat(eligibilityService.evaluate(profile))
                .containsExactlyInAnyOrder("Birth date is required", "Weight is required");
    }
}
