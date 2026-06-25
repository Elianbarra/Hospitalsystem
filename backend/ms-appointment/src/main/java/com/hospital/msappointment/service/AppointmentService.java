package com.hospital.msappointment.service;

import com.hospital.msappointment.dto.request.CreateAppointmentRequestDTO;
import com.hospital.msappointment.dto.request.UpdateAppointmentRequestDTO;
import com.hospital.msappointment.dto.response.AppointmentResponseDTO;
import com.hospital.msappointment.entity.Appointment;
import com.hospital.msappointment.entity.enums.AppointmentStatus;
import com.hospital.msappointment.entity.enums.AppointmentType;
import com.hospital.msappointment.entity.enums.CancelledBy;
import com.hospital.msappointment.exception.AppointmentConflictException;
import com.hospital.msappointment.exception.AppointmentNotFoundException;
import com.hospital.msappointment.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Capa de lógica de negocio para gestión de citas.
 * Recibe llamadas del Controller y orquesta el repositorio y los clientes externos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    // ─── Create ──────────────────────────────────────────────────────────────

    public AppointmentResponseDTO createAppointment(CreateAppointmentRequestDTO dto) {
        // La validación de existencia de paciente y doctor la hace el BFF antes de llamar aquí.
        // Verifica que no haya solapamiento de horario para el doctor
        LocalDateTime end = dto.getScheduledAt().plusMinutes(dto.getDurationMinutes());
        boolean overlap = appointmentRepository
                .existsByDoctorIdAndScheduledAtBetweenAndActiveTrueAndStatusNot(
                        dto.getDoctorId(),
                        dto.getScheduledAt(),
                        end,
                        AppointmentStatus.CANCELLED
                );
        if (overlap) {
            throw new AppointmentConflictException("El doctor ya tiene una cita en ese horario");
        }

        AppointmentType apptType = dto.getAppointmentType() != null
                ? dto.getAppointmentType() : AppointmentType.CONSULTA;

        Appointment appointment = Appointment.builder()
                .patientId(dto.getPatientId())
                .doctorId(dto.getDoctorId())
                .scheduledAt(dto.getScheduledAt())
                .durationMinutes(dto.getDurationMinutes())
                .specialty(dto.getSpecialty())
                .appointmentType(apptType)
                .notes(dto.getNotes())
                .build();

        return toResponse(appointmentRepository.save(appointment));
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    public List<AppointmentResponseDTO> getAllAppointments() {
        return appointmentRepository.findByActiveTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public AppointmentResponseDTO getAppointmentById(UUID id) {
        return appointmentRepository.findByIdAndActiveTrue(id)
                .map(this::toResponse)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
    }

    public List<AppointmentResponseDTO> getByPatient(UUID patientId) {
        return appointmentRepository.findByPatientIdAndActiveTrue(patientId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AppointmentResponseDTO> getByDoctor(UUID doctorId) {
        return appointmentRepository.findByDoctorIdAndActiveTrue(doctorId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    public AppointmentResponseDTO updateAppointment(UUID id, UpdateAppointmentRequestDTO dto) {
        Appointment appointment = appointmentRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));

        if (dto.getScheduledAt() != null)    appointment.setScheduledAt(dto.getScheduledAt());
        if (dto.getDurationMinutes() != null) appointment.setDurationMinutes(dto.getDurationMinutes());
        if (dto.getStatus() != null)          appointment.setStatus(dto.getStatus());
        if (dto.getNotes() != null)           appointment.setNotes(dto.getNotes());

        return toResponse(appointmentRepository.save(appointment));
    }

    // ─── Cancel / Delete ──────────────────────────────────────────────────────

    public AppointmentResponseDTO cancelAppointment(UUID id) {
        Appointment appointment = appointmentRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
        appointment.setStatus(AppointmentStatus.CANCELLED);
        return toResponse(appointmentRepository.save(appointment));
    }

    /**
     * Cancela la cita por decisión del MÉDICO.
     * El BFF es responsable de orquestar la reasignación automática al siguiente
     * en la lista de espera (esta capa solo registra la cancelación).
     */
    public AppointmentResponseDTO cancelByDoctor(UUID id) {
        Appointment appointment = appointmentRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledBy(CancelledBy.DOCTOR);
        log.info("Cita {} cancelada por DOCTOR — BFF orquestará reasignación a especialidad {}",
                id, appointment.getSpecialty());
        return toResponse(appointmentRepository.save(appointment));
    }

    /**
     * Cancela la cita por decisión del PACIENTE.
     * El BFF es responsable de llamar a ms-waitlist para mover al paciente al
     * final de la cola (requeueToEnd). Esta capa solo registra la cancelación.
     */
    public AppointmentResponseDTO cancelByPatient(UUID id) {
        Appointment appointment = appointmentRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledBy(CancelledBy.PATIENT);
        log.info("Cita {} cancelada por PACIENTE — BFF orquestará reencola en lista de espera", id);
        return toResponse(appointmentRepository.save(appointment));
    }

    public void deleteAppointment(UUID id) {
        Appointment appointment = appointmentRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
        appointment.setActive(false);
        appointmentRepository.save(appointment);
    }

    // ─── Mapper ───────────────────────────────────────────────────────────────

    private AppointmentResponseDTO toResponse(Appointment a) {
        return AppointmentResponseDTO.builder()
                .id(a.getId())
                .patientId(a.getPatientId())
                .doctorId(a.getDoctorId())
                .scheduledAt(a.getScheduledAt())
                .durationMinutes(a.getDurationMinutes())
                .specialty(a.getSpecialty())
                .appointmentType(a.getAppointmentType())
                .status(a.getStatus())
                .cancelledBy(a.getCancelledBy())
                .notes(a.getNotes())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
