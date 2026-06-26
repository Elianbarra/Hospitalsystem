package cl.rednorte.bff.service;

import cl.rednorte.bff.dto.request.CreateAppointmentRequestDTO;
import cl.rednorte.bff.dto.request.UpdateWaitlistEntryRequestDTO;
import cl.rednorte.bff.dto.response.AppointmentResponseDTO;
import cl.rednorte.bff.dto.response.ReassignmentResultDTO;
import cl.rednorte.bff.dto.response.WaitlistEntryResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates automatic reassignment of appointments.
 *
 * Rules:
 *  - DOCTOR cancels → free slot → BFF finds next patient in waitlist
 *    → creates new appointment → marks waitlist entry as OFFERED.
 *  - PATIENT cancels → appointment lost → BFF requeues patient to END of waitlist.
 *
 * This class only orchestrates; domain logic lives in each microservice.
 */
@Service
public class ReassignmentService {

    private static final Logger log = LoggerFactory.getLogger(ReassignmentService.class);

    private final AppointmentService appointmentService;
    private final WaitlistService waitlistService;

    private static final Map<String, String> APPT_TO_WAITLIST_SPECIALTY = Map.ofEntries(
        Map.entry("CARDIOLOGY",       "CARDIOLOGIA"),
        Map.entry("NEUROLOGY",        "NEUROLOGIA"),
        Map.entry("GENERAL",          "MEDICINA_GENERAL"),
        Map.entry("PEDIATRICS",       "PEDIATRIA"),
        Map.entry("ORTHOPEDICS",      "TRAUMATOLOGIA"),
        Map.entry("TRAUMATOLOGY",     "TRAUMATOLOGIA"),
        Map.entry("DERMATOLOGY",      "DERMATOLOGIA"),
        Map.entry("GYNECOLOGY",       "GINECOLOGIA"),
        Map.entry("OPHTHALMOLOGY",    "OFTALMOLOGIA"),
        Map.entry("PSYCHIATRY",       "PSIQUIATRIA"),
        Map.entry("INTERNAL_MEDICINE","MEDICINA_GENERAL"),
        Map.entry("EMERGENCY",        "MEDICINA_GENERAL")
    );

    public ReassignmentService(AppointmentService appointmentService, WaitlistService waitlistService) {
        this.appointmentService = appointmentService;
        this.waitlistService = waitlistService;
    }

    public ReassignmentResultDTO cancelByDoctor(String appointmentId, String waitlistEntryId) {
        AppointmentResponseDTO cancelled = appointmentService.cancelByDoctor(appointmentId);
        log.info("Appointment {} cancelled by DOCTOR — specialty={}, doctor={}",
                appointmentId, cancelled.specialty(), cancelled.doctorId());

        String waitlistSpecialty = APPT_TO_WAITLIST_SPECIALTY.getOrDefault(
                cancelled.specialty(), cancelled.specialty());
        WaitlistEntryResponseDTO next = null;
        if (waitlistEntryId != null && !waitlistEntryId.isBlank()) {
            next = waitlistService.getById(waitlistEntryId);
        } else {
            next = waitlistService.getNextForSpecialty(waitlistSpecialty);
        }

        if (next == null) {
            log.info("No patients in waitlist for specialty={} — slot freed with no reassignment",
                    cancelled.specialty());
            return new ReassignmentResultDTO(cancelled, null, null, "HORA_LIBERADA_SIN_REASIGNAR");
        }

        LocalDateTime nextSlot = nextAvailableSlot(cancelled.doctorId(), cancelled.scheduledAt());
        String apptType = next.appointmentType() != null ? next.appointmentType() : "CONSULTA";

        CreateAppointmentRequestDTO newAppt = new CreateAppointmentRequestDTO(
                next.patientId(),
                cancelled.doctorId(),
                cancelled.specialty(),
                apptType,
                nextSlot,
                "Automatic reassignment due to doctor cancellation"
        );

        AppointmentResponseDTO reassigned = appointmentService.create(newAppt);
        log.info("New appointment {} created for patient {} — slot={}",
                reassigned.id(), next.patientId(), nextSlot);

        WaitlistEntryResponseDTO offered = waitlistService.markAsOffered(next.id());
        log.info("Waitlist entry {} marked as OFFERED for patient {}", next.id(), next.patientId());

        return new ReassignmentResultDTO(cancelled, reassigned, offered, "REASIGNADO");
    }

    public ReassignmentResultDTO cancelByPatient(String appointmentId, String waitlistEntryId) {
        AppointmentResponseDTO cancelled = appointmentService.cancelByPatient(appointmentId);
        log.info("Appointment {} cancelled by PATIENT — patient={}", appointmentId, cancelled.patientId());

        WaitlistEntryResponseDTO requeued = null;
        if (waitlistEntryId != null && !waitlistEntryId.isBlank()) {
            requeued = waitlistService.requeueToEnd(waitlistEntryId);
            log.info("Patient {} requeued to end of waitlist for specialty={}",
                    cancelled.patientId(), cancelled.specialty());
        } else {
            log.warn("No waitlistEntryId provided — patient {} was not requeued", cancelled.patientId());
        }

        return new ReassignmentResultDTO(cancelled, null, requeued, "PACIENTE_REUBICADO_AL_FINAL");
    }

    private LocalDateTime nextAvailableSlot(String doctorId, LocalDateTime fromHint) {
        List<AppointmentResponseDTO> doctorAppts = appointmentService.getByDoctor(doctorId);

        LocalDateTime candidate = fromHint != null
                ? fromHint.plusMinutes(30)
                : LocalDateTime.now().plusMinutes(30);

        int mins = candidate.getMinute();
        if (mins > 0 && mins <= 30) {
            candidate = candidate.withMinute(30).withSecond(0).withNano(0);
        } else if (mins > 30) {
            candidate = candidate.plusHours(1).withMinute(0).withSecond(0).withNano(0);
        }

        for (int attempt = 0; attempt < 30 * 16; attempt++) {
            LocalDate day = candidate.toLocalDate();
            int dow = day.getDayOfWeek().getValue();
            if (dow >= 6) {
                candidate = day.plusDays(dow == 6 ? 2 : 1).atTime(LocalTime.of(8, 0));
                continue;
            }
            if (candidate.toLocalTime().isBefore(LocalTime.of(8, 0))) {
                candidate = candidate.toLocalDate().atTime(LocalTime.of(8, 0));
            }
            if (candidate.toLocalTime().isAfter(LocalTime.of(16, 30))) {
                candidate = candidate.toLocalDate().plusDays(1).atTime(LocalTime.of(8, 0));
                continue;
            }

            LocalDateTime finalCandidate = candidate;
            boolean conflict = doctorAppts.stream()
                    .filter(a -> !"CANCELLED".equals(a.status()))
                    .anyMatch(a -> {
                        LocalDateTime start = a.scheduledAt();
                        LocalDateTime end = start.plusMinutes(30);
                        return !finalCandidate.isBefore(start) && finalCandidate.isBefore(end);
                    });

            if (!conflict) return candidate;
            candidate = candidate.plusMinutes(30);
        }

        return LocalDate.now().plusDays(1).atTime(LocalTime.of(8, 0));
    }
}
