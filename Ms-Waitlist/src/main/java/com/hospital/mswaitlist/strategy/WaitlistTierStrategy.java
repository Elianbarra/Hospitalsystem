package com.hospital.mswaitlist.strategy;

import com.hospital.mswaitlist.domain.WaitlistTier;

import java.util.Set;

public interface WaitlistTierStrategy {

    Set<String> supportedValues();

    WaitlistTier resolveTier();
}
