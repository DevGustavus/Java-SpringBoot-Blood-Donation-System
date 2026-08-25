package com.gustavo.blood_donation_system.user.application;

import com.gustavo.blood_donation_system.shared.NotFoundException;
import com.gustavo.blood_donation_system.user.domain.UserProfile;
import com.gustavo.blood_donation_system.user.infrastructure.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetUserProfileUseCase {

    private final UserProfileRepository userProfileRepository;

    @Transactional(readOnly = true)
    public UserProfile execute(String email) {
        return userProfileRepository.findByUserEmailWithUser(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
