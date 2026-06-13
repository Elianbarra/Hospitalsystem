package com.hospital.mswaitlist.service;

import com.hospital.mswaitlist.dto.CreateWaitlistRequest;
import com.hospital.mswaitlist.factory.WaitlistEntryFactory;
import com.hospital.mswaitlist.repository.InMemoryWaitlistRepository;
import com.hospital.mswaitlist.strategy.WaitlistTierStrategyResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WaitlistServiceTest {

    private WaitlistService waitlistService;

    @BeforeEach
    void setUp() {
        waitlistService = new WaitlistService(
                new InMemoryWaitlistRepository(),
                new WaitlistEntryFactory(),
                new WaitlistTierStrategyResolver(java.util.List.of(
                        new com.hospital.mswaitlist.strategy.StandardWaitlistTierStrategy(),
                        new com.hospital.mswaitlist.strategy.PriorityWaitlistTierStrategy()
                ))
        );
    }

    @Test
    void shouldCreateWaitlistEntry() {
        var response = waitlistService.create(new CreateWaitlistRequest("Lucia", "lucia@example.com", "priority"));

        assertThat(response.email()).isEqualTo("lucia@example.com");
        assertThat(response.tier().name()).isEqualTo("PRIORITY");
        assertThat(waitlistService.findAll()).hasSize(1);
    }
}
