package com.hospital.mswaitlist.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record WaitlistEntry(
        UUID id,
        String name,
        String email,
        WaitlistTier tier,
        WaitlistStatus status,
        OffsetDateTime createdAt
) {
}
