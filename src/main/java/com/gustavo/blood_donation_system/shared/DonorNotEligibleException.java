package com.gustavo.blood_donation_system.shared;

import java.util.List;

public class DonorNotEligibleException extends RuntimeException {

    private final List<String> reasons;

    public DonorNotEligibleException(List<String> reasons) {
        super("Donor is not eligible to donate blood");
        this.reasons = List.copyOf(reasons);
    }

    public List<String> getReasons() {
        return reasons;
    }
}
