package com.gustavo.blood_donation_system.donor.application;

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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisableDonorAvailabilityUseCaseTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private DisableDonorAvailabilityUseCase disableDonorAvailabilityUseCase;

    @Test
    void shouldDeactivateAvailability() {
        User user = User.create("donor@example.com", "hash", UserRole.USER);
        UserProfile profile = UserProfile.create(user, "Donor");
        profile.activateAvailability();
        when(userProfileRepository.findByUserEmailWithUser("donor@example.com"))
                .thenReturn(Optional.of(profile));

        UserProfile result = disableDonorAvailabilityUseCase.execute("donor@example.com");

        assertThat(result.isAvailable()).isFalse();
    }

    @Test
    void shouldThrowNotFoundWhenUserDoesNotExist() {
        when(userProfileRepository.findByUserEmailWithUser("ghost@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> disableDonorAvailabilityUseCase.execute("ghost@example.com"))
                .isInstanceOf(NotFoundException.class);
    }
}
