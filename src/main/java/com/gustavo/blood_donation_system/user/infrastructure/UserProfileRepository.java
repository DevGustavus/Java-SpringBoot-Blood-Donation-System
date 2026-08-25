package com.gustavo.blood_donation_system.user.infrastructure;

import com.gustavo.blood_donation_system.user.domain.BloodType;
import com.gustavo.blood_donation_system.user.domain.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    @Query("select p from UserProfile p join fetch p.user u where u.email = :email")
    Optional<UserProfile> findByUserEmailWithUser(@Param("email") String email);

    @Query("""
            select p from UserProfile p
            where p.available = true
              and (:bloodType is null or p.bloodType = :bloodType)
              and (:city is null or lower(p.city) = lower(cast(:city as string)))
              and (:state is null or upper(p.state) = upper(cast(:state as string)))
              and (:zipCode is null or replace(p.zipCode, '-', '') = :zipCode)
            """)
    Page<UserProfile> searchAvailableDonors(@Param("bloodType") BloodType bloodType,
                                            @Param("city") String city,
                                            @Param("state") String state,
                                            @Param("zipCode") String zipCode,
                                            Pageable pageable);

    @Query("select p from UserProfile p where p.id = :id and p.available = true")
    Optional<UserProfile> findAvailableById(@Param("id") Long id);
}
