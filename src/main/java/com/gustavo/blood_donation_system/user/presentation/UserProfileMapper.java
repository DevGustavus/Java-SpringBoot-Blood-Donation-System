package com.gustavo.blood_donation_system.user.presentation;

import com.gustavo.blood_donation_system.user.domain.UserProfile;
import org.springframework.stereotype.Component;

@Component
public class UserProfileMapper {

    public UserProfileResponse toResponse(UserProfile profile) {
        return new UserProfileResponse(
                profile.getUser().getId(),
                profile.getUser().getEmail(),
                profile.getFullName(),
                profile.getPhone(),
                profile.getBirthDate(),
                profile.getWeightKg(),
                profile.getHeightCm(),
                profile.getBloodType(),
                profile.getAddress(),
                profile.getCity(),
                profile.getState(),
                profile.getZipCode());
    }
}
