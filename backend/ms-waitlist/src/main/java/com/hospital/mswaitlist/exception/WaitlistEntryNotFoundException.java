package com.hospital.mswaitlist.exception;

import java.util.UUID;

public class WaitlistEntryNotFoundException extends RuntimeException {
    public WaitlistEntryNotFoundException(UUID id) {
        super("Entrada en lista de espera no encontrada: " + id);
    }
}
