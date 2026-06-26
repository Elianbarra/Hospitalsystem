package com.hospital.mswaitlist.dto.response;

import com.hospital.mswaitlist.entity.enums.AppointmentType;
import com.hospital.mswaitlist.entity.enums.Priority;
import com.hospital.mswaitlist.entity.enums.Specialty;
import com.hospital.mswaitlist.entity.enums.WaitlistStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaitlistEntryResponseDTO {

    private UUID id;
    private UUID patientId;
    private Specialty specialty;
    private AppointmentType appointmentType;
    private Priority priority;
    private Boolean vitalRisk;
    private WaitlistStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime requeuedAt;
    private LocalDateTime updatedAt;
}
