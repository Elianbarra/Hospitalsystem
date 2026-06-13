package com.hospital.mswaitlist.service;

import com.hospital.mswaitlist.domain.WaitlistEntry;
import com.hospital.mswaitlist.dto.CreateWaitlistRequest;
import com.hospital.mswaitlist.dto.WaitlistResponse;
import com.hospital.mswaitlist.factory.WaitlistEntryFactory;
import com.hospital.mswaitlist.repository.WaitlistRepository;
import com.hospital.mswaitlist.strategy.WaitlistTierStrategyResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class WaitlistService {

    private final WaitlistRepository repository;
    private final WaitlistEntryFactory factory;
    private final WaitlistTierStrategyResolver tierStrategyResolver;

    public WaitlistService(
            WaitlistRepository repository,
            WaitlistEntryFactory factory,
            WaitlistTierStrategyResolver tierStrategyResolver
    ) {
        this.repository = repository;
        this.factory = factory;
        this.tierStrategyResolver = tierStrategyResolver;
    }

    public WaitlistResponse create(CreateWaitlistRequest request) {
        repository.findByEmail(request.email())
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered in waitlist");
                });

        WaitlistEntry entry = factory.create(request, tierStrategyResolver.resolve(request.tier()));
        return toResponse(repository.save(entry));
    }

    public List<WaitlistResponse> findAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public WaitlistResponse findById(UUID id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Waitlist entry not found"));
    }

    private WaitlistResponse toResponse(WaitlistEntry entry) {
        return new WaitlistResponse(
                entry.id(),
                entry.name(),
                entry.email(),
                entry.tier(),
                entry.status(),
                entry.createdAt()
        );
    }
}
