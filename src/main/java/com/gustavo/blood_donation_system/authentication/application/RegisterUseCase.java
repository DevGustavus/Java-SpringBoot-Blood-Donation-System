package com.gustavo.blood_donation_system.authentication.application;

import com.gustavo.blood_donation_system.shared.ConflictException;
import com.gustavo.blood_donation_system.user.domain.User;
import com.gustavo.blood_donation_system.user.domain.UserProfile;
import com.gustavo.blood_donation_system.user.domain.UserRole;
import com.gustavo.blood_donation_system.user.infrastructure.UserProfileRepository;
import com.gustavo.blood_donation_system.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterUseCase {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisteredUser execute(String name, String email, String rawPassword) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Email already registered");
        }
        User user = User.create(normalizedEmail, passwordEncoder.encode(rawPassword), UserRole.USER);
        userRepository.save(user);
        UserProfile profile = UserProfile.create(user, name);
        userProfileRepository.save(profile);
        return new RegisteredUser(user.getId(), name, normalizedEmail);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    public record RegisteredUser(Long id, String name, String email) {
    }
}
