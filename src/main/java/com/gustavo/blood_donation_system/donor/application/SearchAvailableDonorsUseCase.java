package com.gustavo.blood_donation_system.donor.application;

import com.gustavo.blood_donation_system.user.domain.BloodType;
import com.gustavo.blood_donation_system.user.domain.UserProfile;
import com.gustavo.blood_donation_system.user.infrastructure.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchAvailableDonorsUseCase {

    private final UserProfileRepository userProfileRepository;

    @Transactional(readOnly = true)
    public Page<UserProfile> execute(SearchDonorsCommand command, Pageable pageable) {
        if (!command.available()) {
            return Page.empty(pageable);
        }
        return userProfileRepository.searchAvailableDonors(
                command.bloodType(), command.city(), command.state(), command.zipCode(), pageable);
    }

    public record SearchDonorsCommand(
            BloodType bloodType, String city, String state, String zipCode, boolean available) {
    }
}
