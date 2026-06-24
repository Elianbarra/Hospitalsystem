package com.hospital.mswaitlist.dto.request;

import com.hospital.mswaitlist.entity.enums.Priority;
import com.hospital.mswaitlist.entity.enums.Specialty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWaitlistEntryRequestDTO {

    @NotNull(message = "El id del paciente es obligatorio")
    private UUID patientId;

    @NotNull(message = "La especialidad es obligatoria")
    private Specialty specialty;

    @Builder.Default
    private Priority priority = Priority.NORMAL;

    private String notes;
}
