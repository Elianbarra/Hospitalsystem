package com.hospital.mswaitlist.strategy;

import com.hospital.mswaitlist.domain.WaitlistTier;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class StandardWaitlistTierStrategy implements WaitlistTierStrategy {

    @Override
    public Set<String> supportedValues() {
        return Set.of("STANDARD", "BASIC", "DEFAULT");
    }

    @Override
    public WaitlistTier resolveTier() {
        return WaitlistTier.STANDARD;
    }
}
