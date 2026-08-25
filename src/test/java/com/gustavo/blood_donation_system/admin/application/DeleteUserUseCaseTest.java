package com.gustavo.blood_donation_system.admin.application;

import com.gustavo.blood_donation_system.shared.ConflictException;
import com.gustavo.blood_donation_system.shared.NotFoundException;
import com.gustavo.blood_donation_system.user.domain.User;
import com.gustavo.blood_donation_system.user.domain.UserRole;
import com.gustavo.blood_donation_system.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DeleteUserUseCase deleteUserUseCase;

    @Test
    void shouldDeleteTargetUser() {
        User target = User.create("victim@example.com", "hash", UserRole.USER);
        when(userRepository.findById(10L)).thenReturn(Optional.of(target));

        deleteUserUseCase.execute(10L, "admin@example.com");

        verify(userRepository).delete(target);
    }

    @Test
    void shouldRejectDeletingOwnAccount() {
        User admin = User.create("admin@example.com", "hash", UserRole.ADMIN);
        when(userRepository.findById(10L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> deleteUserUseCase.execute(10L, "admin@example.com"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("own account");
        verify(userRepository, never()).delete(admin);
    }

    @Test
    void shouldThrowNotFoundWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteUserUseCase.execute(99L, "admin@example.com"))
                .isInstanceOf(NotFoundException.class);
        verify(userRepository, never()).delete(org.mockito.ArgumentMatchers.any());
    }
}
