package com.hospital.mswaitlist.dto.request;

import com.hospital.mswaitlist.entity.enums.AppointmentType;
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

    @NotNull(message = "El tipo de atención es obligatorio")
    @Builder.Default
    private AppointmentType appointmentType = AppointmentType.CONSULTA;

    @Builder.Default
    private Priority priority = Priority.NORMAL;

    @Builder.Default
    private Boolean vitalRisk = false;

    private String notes;
}
