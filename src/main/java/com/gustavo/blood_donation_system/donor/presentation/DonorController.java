package com.gustavo.blood_donation_system.donor.presentation;

import com.gustavo.blood_donation_system.donor.application.DisableDonorAvailabilityUseCase;
import com.gustavo.blood_donation_system.donor.application.EnableDonorAvailabilityUseCase;
import com.gustavo.blood_donation_system.donor.application.GetDonorUseCase;
import com.gustavo.blood_donation_system.donor.application.SearchAvailableDonorsUseCase;
import com.gustavo.blood_donation_system.user.domain.BloodType;
import com.gustavo.blood_donation_system.user.domain.UserProfile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/donors")
@RequiredArgsConstructor
@Validated
public class DonorController {

    private final SearchAvailableDonorsUseCase searchAvailableDonorsUseCase;
    private final GetDonorUseCase getDonorUseCase;
    private final EnableDonorAvailabilityUseCase enableDonorAvailabilityUseCase;
    private final DisableDonorAvailabilityUseCase disableDonorAvailabilityUseCase;
    private final DonorMapper donorMapper;

    @GetMapping
    public Page<DonorResponse> search(@RequestParam(required = false) BloodType bloodType,
                                      @RequestParam(required = false) String city,
                                      @RequestParam(required = false) String state,
                                      @RequestParam(required = false) String zipCode,
                                      @RequestParam(defaultValue = "true") boolean available,
                                      @RequestParam(defaultValue = "0") @Min(0) int page,
                                      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        String trimmedCity = StringUtils.hasText(city) ? city.trim() : null;
        String trimmedState = StringUtils.hasText(state) ? state.trim() : null;
        String normalizedZipCode = StringUtils.hasText(zipCode) ? zipCode.replace("-", "").trim() : null;
        Page<UserProfile> donors = searchAvailableDonorsUseCase.execute(
                new SearchAvailableDonorsUseCase.SearchDonorsCommand(
                        bloodType, trimmedCity, trimmedState, normalizedZipCode, available),
                PageRequest.of(page, size));
        return donors.map(donorMapper::toResponse);
    }

    @GetMapping("/{id}")
    public DonorResponse getById(@PathVariable Long id) {
        UserProfile profile = getDonorUseCase.execute(id);
        return donorMapper.toResponse(profile);
    }

    @PatchMapping("/me/availability")
    public DonorAvailabilityResponse updateAvailability(@AuthenticationPrincipal Jwt jwt,
                                                        @Valid @RequestBody UpdateAvailabilityRequest request) {
        UserProfile profile = request.available()
                ? enableDonorAvailabilityUseCase.execute(jwt.getSubject())
                : disableDonorAvailabilityUseCase.execute(jwt.getSubject());
        return new DonorAvailabilityResponse(profile.isAvailable());
    }
}
