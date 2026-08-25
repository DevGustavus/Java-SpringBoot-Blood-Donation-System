package com.gustavo.blood_donation_system.donor.presentation;

import com.gustavo.blood_donation_system.user.domain.UserProfile;
import org.springframework.stereotype.Component;

@Component
public class DonorMapper {

    public DonorResponse toResponse(UserProfile profile) {
        return new DonorResponse(
                profile.getId(),
                profile.getFullName(),
                profile.getBloodType(),
                profile.getCity(),
                profile.getState(),
                profile.getZipCode());
    }
}
