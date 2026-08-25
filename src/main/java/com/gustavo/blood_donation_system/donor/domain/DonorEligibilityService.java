package com.gustavo.blood_donation_system.donor.domain;

import com.gustavo.blood_donation_system.user.domain.UserProfile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Service
public class DonorEligibilityService {

    private static final int MIN_AGE_YEARS = 18;
    private static final int MAX_AGE_YEARS = 69;
    private static final BigDecimal MIN_WEIGHT_KG = new BigDecimal("50");

    public boolean isEligible(UserProfile profile) {
        return evaluate(profile).isEmpty();
    }

    public List<String> evaluate(UserProfile profile) {
        List<String> reasons = new ArrayList<>();
        LocalDate birthDate = profile.getBirthDate();
        if (birthDate == null) {
            reasons.add("Birth date is required");
        } else {
            int age = Period.between(birthDate, LocalDate.now()).getYears();
            if (age < MIN_AGE_YEARS) {
                reasons.add("Donor must be at least 18 years old");
            }
            if (age > MAX_AGE_YEARS) {
                reasons.add("Donor must be at most 69 years old");
            }
        }
        BigDecimal weight = profile.getWeightKg();
        if (weight == null) {
            reasons.add("Weight is required");
        } else if (weight.compareTo(MIN_WEIGHT_KG) < 0) {
            reasons.add("Donor must weigh at least 50 kg");
        }
        return reasons;
    }
}
