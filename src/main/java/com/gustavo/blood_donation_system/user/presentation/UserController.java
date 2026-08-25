package com.gustavo.blood_donation_system.user.presentation;

import com.gustavo.blood_donation_system.user.application.GetUserProfileUseCase;
import com.gustavo.blood_donation_system.user.application.UpdateUserProfileUseCase;
import com.gustavo.blood_donation_system.user.domain.UserProfile;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final GetUserProfileUseCase getUserProfileUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final UserProfileMapper userProfileMapper;

    @GetMapping("/me")
    public UserProfileResponse getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        UserProfile profile = getUserProfileUseCase.execute(jwt.getSubject());
        return userProfileMapper.toResponse(profile);
    }

    @PutMapping("/me")
    public UserProfileResponse updateMyProfile(@AuthenticationPrincipal Jwt jwt,
                                               @Valid @RequestBody UpdateUserProfileRequest request) {
        UpdateUserProfileUseCase.UpdateProfileCommand command =
                new UpdateUserProfileUseCase.UpdateProfileCommand(
                        request.fullName(), request.phone(), request.birthDate(), request.weightKg(),
                        request.heightCm(), request.bloodType(), request.address(), request.city(),
                        request.state(), request.zipCode());
        UserProfile profile = updateUserProfileUseCase.execute(jwt.getSubject(), command);
        return userProfileMapper.toResponse(profile);
    }
}
