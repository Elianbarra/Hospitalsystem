package com.hospital.mswaitlist.repository;

import com.hospital.mswaitlist.entity.WaitlistEntry;
import com.hospital.mswaitlist.entity.enums.Specialty;
import com.hospital.mswaitlist.entity.enums.WaitlistStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WaitlistRepository extends JpaRepository<WaitlistEntry, UUID> {

    List<WaitlistEntry> findByActiveTrueOrderByPriorityDescCreatedAtAsc();

    List<WaitlistEntry> findByPatientIdAndActiveTrueOrderByCreatedAtAsc(UUID patientId);

    List<WaitlistEntry> findBySpecialtyAndActiveTrueOrderByPriorityDescCreatedAtAsc(Specialty specialty);

    List<WaitlistEntry> findByStatusAndActiveTrueOrderByPriorityDescCreatedAtAsc(WaitlistStatus status);

    Optional<WaitlistEntry> findByIdAndActiveTrue(UUID id);
}
