package com.gustavo.blood_donation_system.donor.application;

import com.gustavo.blood_donation_system.donor.domain.DonorEligibilityService;
import com.gustavo.blood_donation_system.shared.DonorNotEligibleException;
import com.gustavo.blood_donation_system.shared.NotFoundException;
import com.gustavo.blood_donation_system.user.domain.UserProfile;
import com.gustavo.blood_donation_system.user.infrastructure.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnableDonorAvailabilityUseCase {

    private final UserProfileRepository userProfileRepository;
    private final DonorEligibilityService donorEligibilityService;

    @Transactional
    public UserProfile execute(String email) {
        UserProfile profile = userProfileRepository.findByUserEmailWithUser(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        List<String> reasons = donorEligibilityService.evaluate(profile);
        if (!reasons.isEmpty()) {
            throw new DonorNotEligibleException(reasons);
        }
        profile.activateAvailability();
        return profile;
    }
}
