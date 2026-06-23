package com.hospital.mswaitlist.repository;

import com.hospital.mswaitlist.entity.WaitlistEntry;
import com.hospital.mswaitlist.entity.enums.Specialty;
import com.hospital.mswaitlist.entity.enums.WaitlistStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de lista de espera.
 * El ordenamiento correcto de cola (vitalRisk → priority → requeuedAt)
 * se aplica en la capa de servicio con sort Java para mayor claridad y portabilidad.
 */
@Repository
public interface WaitlistRepository extends JpaRepository<WaitlistEntry, UUID> {

    List<WaitlistEntry> findByActiveTrue();

    List<WaitlistEntry> findByPatientIdAndActiveTrueOrderByRequeuedAtAsc(UUID patientId);

    List<WaitlistEntry> findBySpecialtyAndActiveTrue(Specialty specialty);

    List<WaitlistEntry> findByStatusAndActiveTrue(WaitlistStatus status);

    Optional<WaitlistEntry> findByIdAndActiveTrue(UUID id);

    List<WaitlistEntry> findByPatientIdAndSpecialtyAndActiveTrueAndStatusIn(
            UUID patientId, Specialty specialty, List<WaitlistStatus> statuses);
}
