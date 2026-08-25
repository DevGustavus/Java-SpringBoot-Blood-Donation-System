package com.gustavo.blood_donation_system.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "user_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(length = 20)
    private String phone;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "height_cm", precision = 5, scale = 2)
    private BigDecimal heightCm;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_type", length = 15)
    private BloodType bloodType;

    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 2)
    private String state;

    @Column(name = "zip_code", length = 9)
    private String zipCode;

    @Column(nullable = false)
    private boolean available;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    private UserProfile(User user, String fullName) {
        this.user = user;
        this.fullName = fullName;
    }

    public static UserProfile create(User user, String fullName) {
        UserProfile profile = new UserProfile(user, fullName);
        profile.createdAt = Instant.now();
        profile.updatedAt = Instant.now();
        return profile;
    }

    public void update(String fullName, String phone, LocalDate birthDate, BigDecimal weightKg,
                       BigDecimal heightCm, BloodType bloodType, String address, String city,
                       String state, String zipCode) {
        this.fullName = fullName;
        this.phone = phone;
        this.birthDate = birthDate;
        this.weightKg = weightKg;
        this.heightCm = heightCm;
        this.bloodType = bloodType;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.updatedAt = Instant.now();
    }

    public void activateAvailability() {
        this.available = true;
        this.updatedAt = Instant.now();
    }

    public void deactivateAvailability() {
        this.available = false;
        this.updatedAt = Instant.now();
    }
}
