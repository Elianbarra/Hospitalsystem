package com.hospital.mswaitlist.factory;

import com.hospital.mswaitlist.domain.WaitlistTier;
import com.hospital.mswaitlist.dto.CreateWaitlistRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WaitlistEntryFactoryTest {

    private final WaitlistEntryFactory factory = new WaitlistEntryFactory();

    @Test
    void shouldNormalizeEmailAndName() {
        var request = new CreateWaitlistRequest("  Ana Lopez  ", "  ANA@EXAMPLE.COM  ", null);

        var entry = factory.create(request, WaitlistTier.STANDARD);

        assertThat(entry.name()).isEqualTo("Ana Lopez");
        assertThat(entry.email()).isEqualTo("ana@example.com");
        assertThat(entry.tier()).isEqualTo(WaitlistTier.STANDARD);
    }
}
