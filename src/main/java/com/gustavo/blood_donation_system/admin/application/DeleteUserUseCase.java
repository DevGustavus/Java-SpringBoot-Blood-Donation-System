package com.gustavo.blood_donation_system.admin.application;

import com.gustavo.blood_donation_system.shared.ConflictException;
import com.gustavo.blood_donation_system.shared.NotFoundException;
import com.gustavo.blood_donation_system.user.domain.User;
import com.gustavo.blood_donation_system.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteUserUseCase {

    private final UserRepository userRepository;

    @Transactional
    public void execute(Long targetUserId, String currentUserEmail) {
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (target.getEmail().equals(currentUserEmail)) {
            throw new ConflictException("Admin cannot delete their own account");
        }
        userRepository.delete(target);
    }
}
