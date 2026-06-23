package com.hospital.mswaitlist.dto.request;

import com.hospital.mswaitlist.entity.enums.Priority;
import com.hospital.mswaitlist.entity.enums.WaitlistStatus;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWaitlistEntryRequestDTO {

    private WaitlistStatus status;
    private Priority priority;
    /** Riesgo vital severo — actualizable en cualquier momento por el médico */
    private Boolean vitalRisk;
    private String notes;
}
