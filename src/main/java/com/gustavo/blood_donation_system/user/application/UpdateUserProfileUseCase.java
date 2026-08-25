package com.gustavo.blood_donation_system.user.application;

import com.gustavo.blood_donation_system.shared.NotFoundException;
import com.gustavo.blood_donation_system.user.domain.BloodType;
import com.gustavo.blood_donation_system.user.domain.UserProfile;
import com.gustavo.blood_donation_system.user.infrastructure.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UpdateUserProfileUseCase {

    private final UserProfileRepository userProfileRepository;

    @Transactional
    public UserProfile execute(String email, UpdateProfileCommand command) {
        UserProfile profile = userProfileRepository.findByUserEmailWithUser(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        profile.update(command.fullName(), command.phone(), command.birthDate(), command.weightKg(),
                command.heightCm(), command.bloodType(), command.address(), command.city(),
                command.state(), command.zipCode());
        return profile;
    }

    public record UpdateProfileCommand(
            String fullName,
            String phone,
            LocalDate birthDate,
            BigDecimal weightKg,
            BigDecimal heightCm,
            BloodType bloodType,
            String address,
            String city,
            String state,
            String zipCode) {
    }
}
