package com.hospital.mswaitlist.repository;

import com.hospital.mswaitlist.domain.WaitlistEntry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WaitlistRepository {

    WaitlistEntry save(WaitlistEntry entry);

    List<WaitlistEntry> findAll();

    Optional<WaitlistEntry> findById(UUID id);

    Optional<WaitlistEntry> findByEmail(String email);
}
