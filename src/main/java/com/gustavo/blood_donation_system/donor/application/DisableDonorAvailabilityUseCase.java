package com.gustavo.blood_donation_system.donor.application;

import com.gustavo.blood_donation_system.shared.NotFoundException;
import com.gustavo.blood_donation_system.user.domain.UserProfile;
import com.gustavo.blood_donation_system.user.infrastructure.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DisableDonorAvailabilityUseCase {

    private final UserProfileRepository userProfileRepository;

    @Transactional
    public UserProfile execute(String email) {
        UserProfile profile = userProfileRepository.findByUserEmailWithUser(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        profile.deactivateAvailability();
        return profile;
    }
}
