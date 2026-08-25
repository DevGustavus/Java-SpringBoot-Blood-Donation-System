package com.gustavo.blood_donation_system.donor.application;

import com.gustavo.blood_donation_system.donor.domain.DonorEligibilityService;
import com.gustavo.blood_donation_system.shared.DonorNotEligibleException;
import com.gustavo.blood_donation_system.shared.NotFoundException;
import com.gustavo.blood_donation_system.user.domain.User;
import com.gustavo.blood_donation_system.user.domain.UserProfile;
import com.gustavo.blood_donation_system.user.domain.UserRole;
import com.gustavo.blood_donation_system.user.infrastructure.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnableDonorAvailabilityUseCaseTest {

    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private DonorEligibilityService donorEligibilityService;

    @InjectMocks
    private EnableDonorAvailabilityUseCase enableDonorAvailabilityUseCase;

    private UserProfile profile() {
        User user = User.create("donor@example.com", "hash", UserRole.USER);
        return UserProfile.create(user, "Donor");
    }

    @Test
    void shouldActivateAvailabilityWhenEligible() {
        UserProfile profile = profile();
        when(userProfileRepository.findByUserEmailWithUser("donor@example.com"))
                .thenReturn(Optional.of(profile));
        when(donorEligibilityService.evaluate(profile)).thenReturn(List.of());

        UserProfile result = enableDonorAvailabilityUseCase.execute("donor@example.com");

        assertThat(result.isAvailable()).isTrue();
    }

    @Test
    void shouldRejectWhenNotEligible() {
        UserProfile profile = profile();
        when(userProfileRepository.findByUserEmailWithUser("donor@example.com"))
                .thenReturn(Optional.of(profile));
        when(donorEligibilityService.evaluate(profile)).thenReturn(List.of("Donor must weigh at least 50 kg"));

        assertThatThrownBy(() -> enableDonorAvailabilityUseCase.execute("donor@example.com"))
                .isInstanceOf(DonorNotEligibleException.class)
                .satisfies(ex -> assertThat(((DonorNotEligibleException) ex).getReasons())
                        .contains("Donor must weigh at least 50 kg"));
        assertThat(profile.isAvailable()).isFalse();
    }

    @Test
    void shouldThrowNotFoundWhenUserDoesNotExist() {
        when(userProfileRepository.findByUserEmailWithUser("ghost@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> enableDonorAvailabilityUseCase.execute("ghost@example.com"))
                .isInstanceOf(NotFoundException.class);
    }
}
