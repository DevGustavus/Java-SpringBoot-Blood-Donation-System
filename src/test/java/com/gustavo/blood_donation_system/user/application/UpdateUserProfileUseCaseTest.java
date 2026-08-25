package com.gustavo.blood_donation_system.user.application;

import com.gustavo.blood_donation_system.shared.NotFoundException;
import com.gustavo.blood_donation_system.user.domain.BloodType;
import com.gustavo.blood_donation_system.user.domain.User;
import com.gustavo.blood_donation_system.user.domain.UserProfile;
import com.gustavo.blood_donation_system.user.domain.UserRole;
import com.gustavo.blood_donation_system.user.infrastructure.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateUserProfileUseCaseTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private UpdateUserProfileUseCase updateUserProfileUseCase;

    @Test
    void shouldUpdateAllProfileFields() {
        User user = User.create("joao@example.com", "hashed-password", UserRole.USER);
        UserProfile profile = UserProfile.create(user, "João Silva");
        when(userProfileRepository.findByUserEmailWithUser("joao@example.com"))
                .thenReturn(Optional.of(profile));

        UpdateUserProfileUseCase.UpdateProfileCommand command =
                new UpdateUserProfileUseCase.UpdateProfileCommand(
                        "João S. da Silva", "(34) 99999-9999", LocalDate.of(1990, 5, 10),
                        new BigDecimal("75.5"), new BigDecimal("178"), BloodType.O_NEGATIVE,
                        "Rua das Flores, 123", "Uberaba", "MG", "38010-000");

        UserProfile updated = updateUserProfileUseCase.execute("joao@example.com", command);

        assertThat(updated.getFullName()).isEqualTo("João S. da Silva");
        assertThat(updated.getPhone()).isEqualTo("(34) 99999-9999");
        assertThat(updated.getBirthDate()).isEqualTo(LocalDate.of(1990, 5, 10));
        assertThat(updated.getWeightKg()).isEqualByComparingTo("75.5");
        assertThat(updated.getHeightCm()).isEqualByComparingTo("178");
        assertThat(updated.getBloodType()).isEqualTo(BloodType.O_NEGATIVE);
        assertThat(updated.getAddress()).isEqualTo("Rua das Flores, 123");
        assertThat(updated.getCity()).isEqualTo("Uberaba");
        assertThat(updated.getState()).isEqualTo("MG");
        assertThat(updated.getZipCode()).isEqualTo("38010-000");
    }

    @Test
    void shouldThrowNotFoundWhenUserDoesNotExist() {
        when(userProfileRepository.findByUserEmailWithUser("ghost@example.com"))
                .thenReturn(Optional.empty());

        UpdateUserProfileUseCase.UpdateProfileCommand command =
                new UpdateUserProfileUseCase.UpdateProfileCommand(
                        "Ghost", null, LocalDate.of(1990, 1, 1), new BigDecimal("70"),
                        new BigDecimal("170"), BloodType.A_POSITIVE, null, null, null, null);

        assertThatThrownBy(() -> updateUserProfileUseCase.execute("ghost@example.com", command))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }
}
