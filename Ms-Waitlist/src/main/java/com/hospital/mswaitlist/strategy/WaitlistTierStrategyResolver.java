package com.hospital.mswaitlist.strategy;

import com.hospital.mswaitlist.domain.WaitlistTier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class WaitlistTierStrategyResolver {

    private final Map<String, WaitlistTierStrategy> strategyByValue;
    private final WaitlistTierStrategy defaultStrategy;

    public WaitlistTierStrategyResolver(List<WaitlistTierStrategy> strategies) {
        this.strategyByValue = strategies.stream()
                .flatMap(strategy -> strategy.supportedValues().stream()
                        .map(value -> Map.entry(value.toUpperCase(Locale.ROOT), strategy)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (first, ignored) -> first));
        this.defaultStrategy = strategies.stream()
                .filter(strategy -> strategy.supportedValues().contains("STANDARD"))
                .findFirst()
                .orElseThrow();
    }

    public WaitlistTier resolve(String requestedValue) {
        if (requestedValue == null || requestedValue.isBlank()) {
            return defaultStrategy.resolveTier();
        }

        return strategyByValue.getOrDefault(
                requestedValue.trim().toUpperCase(Locale.ROOT),
                defaultStrategy
        ).resolveTier();
    }
}
