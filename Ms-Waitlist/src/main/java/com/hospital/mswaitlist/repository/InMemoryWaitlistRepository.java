package com.hospital.mswaitlist.repository;

import com.hospital.mswaitlist.domain.WaitlistEntry;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryWaitlistRepository implements WaitlistRepository {

    private final ConcurrentMap<UUID, WaitlistEntry> storage = new ConcurrentHashMap<>();

    @Override
    public WaitlistEntry save(WaitlistEntry entry) {
        storage.put(entry.id(), entry);
        return entry;
    }

    @Override
    public List<WaitlistEntry> findAll() {
        return storage.values().stream()
                .sorted(Comparator.comparing(WaitlistEntry::createdAt))
                .toList();
    }

    @Override
    public Optional<WaitlistEntry> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<WaitlistEntry> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        return storage.values().stream()
                .filter(entry -> entry.email().equals(normalizedEmail))
                .findFirst();
    }
}
