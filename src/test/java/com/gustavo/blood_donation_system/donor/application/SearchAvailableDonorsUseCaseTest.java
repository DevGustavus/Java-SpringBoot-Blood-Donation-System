package com.gustavo.blood_donation_system.donor.application;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchAvailableDonorsUseCaseTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private SearchAvailableDonorsUseCase searchAvailableDonorsUseCase;

    private UserProfile donor() {
        User user = User.create("donor@example.com", "hash", UserRole.USER);
        return UserProfile.create(user, "Donor");
    }

    @Test
    void shouldDelegateFiltersAndPaginationToRepository() {
        UserProfile donor = donor();
        PageRequest pageable = PageRequest.of(1, 10);
        SearchAvailableDonorsUseCase.SearchDonorsCommand command =
                new SearchAvailableDonorsUseCase.SearchDonorsCommand(
                        BloodType.O_NEGATIVE, "Uberaba", "MG", "38010000", true);
        when(userProfileRepository.searchAvailableDonors(BloodType.O_NEGATIVE, "Uberaba", "MG", "38010000", pageable))
                .thenReturn(new PageImpl<>(List.of(donor)));

        Page<UserProfile> result = searchAvailableDonorsUseCase.execute(command, pageable);

        assertThat(result.getContent()).containsExactly(donor);
        verify(userProfileRepository).searchAvailableDonors(
                BloodType.O_NEGATIVE, "Uberaba", "MG", "38010000", pageable);
    }

    @Test
    void shouldReturnEmptyPageWithoutQueryingWhenSearchingUnavailableDonors() {
        PageRequest pageable = PageRequest.of(0, 20);
        SearchAvailableDonorsUseCase.SearchDonorsCommand command =
                new SearchAvailableDonorsUseCase.SearchDonorsCommand(null, null, null, null, false);

        Page<UserProfile> result = searchAvailableDonorsUseCase.execute(command, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        verifyNoInteractions(userProfileRepository);
    }
}
