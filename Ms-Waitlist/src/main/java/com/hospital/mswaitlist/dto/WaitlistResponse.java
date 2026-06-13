package com.hospital.mswaitlist.dto;

import com.hospital.mswaitlist.domain.WaitlistStatus;
import com.hospital.mswaitlist.domain.WaitlistTier;

import java.time.OffsetDateTime;
import java.util.UUID;

public record WaitlistResponse(
        UUID id,
        String name,
        String email,
        WaitlistTier tier,
        WaitlistStatus status,
        OffsetDateTime createdAt
) {
}
