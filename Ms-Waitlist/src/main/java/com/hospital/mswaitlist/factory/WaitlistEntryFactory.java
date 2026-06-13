package com.hospital.mswaitlist.factory;

import com.hospital.mswaitlist.domain.WaitlistEntry;
import com.hospital.mswaitlist.domain.WaitlistStatus;
import com.hospital.mswaitlist.domain.WaitlistTier;
import com.hospital.mswaitlist.dto.CreateWaitlistRequest;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
public class WaitlistEntryFactory {

    public WaitlistEntry create(CreateWaitlistRequest request, WaitlistTier tier) {
        return new WaitlistEntry(
                UUID.randomUUID(),
                request.name().trim(),
                request.email().trim().toLowerCase(),
                tier,
                WaitlistStatus.PENDING,
                OffsetDateTime.now(ZoneOffset.UTC)
        );
    }
}
