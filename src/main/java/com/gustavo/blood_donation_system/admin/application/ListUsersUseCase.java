package com.gustavo.blood_donation_system.admin.application;

import com.gustavo.blood_donation_system.user.domain.User;
import com.gustavo.blood_donation_system.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListUsersUseCase {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<User> execute(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
}
