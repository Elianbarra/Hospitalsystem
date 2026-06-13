package com.hospital.mswaitlist.controller;

import com.hospital.mswaitlist.dto.CreateWaitlistRequest;
import com.hospital.mswaitlist.dto.WaitlistResponse;
import com.hospital.mswaitlist.service.WaitlistService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/waitlist")
@Tag(name = "Waitlist", description = "Gestión de lista de espera con estrategias de prioridad")
@SecurityRequirement(name = "bearerAuth")
public class WaitlistController {

    private final WaitlistService waitlistService;

    public WaitlistController(WaitlistService waitlistService) {
        this.waitlistService = waitlistService;
    }

    @PostMapping
    public ResponseEntity<WaitlistResponse> create(@Valid @RequestBody CreateWaitlistRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(waitlistService.create(request));
    }

    @GetMapping
    public List<WaitlistResponse> findAll() {
        return waitlistService.findAll();
    }

    @GetMapping("/{id}")
    public WaitlistResponse findById(@PathVariable UUID id) {
        return waitlistService.findById(id);
    }
}
