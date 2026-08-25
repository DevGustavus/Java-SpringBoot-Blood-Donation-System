package com.gustavo.blood_donation_system.donor.application;

import com.gustavo.blood_donation_system.shared.NotFoundException;
import com.gustavo.blood_donation_system.user.domain.UserProfile;
import com.gustavo.blood_donation_system.user.infrastructure.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetDonorUseCase {

    private final UserProfileRepository userProfileRepository;

    @Transactional(readOnly = true)
    public UserProfile execute(Long donorId) {
        return userProfileRepository.findAvailableById(donorId)
                .orElseThrow(() -> new NotFoundException("Donor not found"));
    }
}
