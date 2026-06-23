package com.hospital.mswaitlist.entity;

import com.hospital.mswaitlist.entity.enums.AppointmentType;
import com.hospital.mswaitlist.entity.enums.Priority;
import com.hospital.mswaitlist.entity.enums.Specialty;
import com.hospital.mswaitlist.entity.enums.WaitlistStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "waitlist_entries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaitlistEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Specialty specialty;

    /** Tipo de atención que el paciente requiere (consulta o cirugía) */
    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_type", nullable = false)
    @Builder.Default
    private AppointmentType appointmentType = AppointmentType.CONSULTA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Priority priority = Priority.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private WaitlistStatus status = WaitlistStatus.WAITING;

    /**
     * Indica riesgo vital severo. Cuando es true, el paciente sube
     * automáticamente al tope de la cola independiente de su prioridad.
     * Actualizable en cualquier momento por el médico.
     */
    @Column(name = "vital_risk", nullable = false)
    @Builder.Default
    private Boolean vitalRisk = false;

    @Column(columnDefinition = "TEXT")
    private String notes;

    /**
     * Timestamp efectivo de posición en cola.
     * Igual a createdAt en inscripción inicial.
     * Se actualiza a NOW() cuando el paciente cancela y vuelve al final de la cola.
     */
    @Column(name = "requeued_at", nullable = false)
    private LocalDateTime requeuedAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
