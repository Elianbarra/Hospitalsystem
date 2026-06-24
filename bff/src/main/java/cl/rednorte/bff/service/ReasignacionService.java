package cl.rednorte.bff.service;

import cl.rednorte.bff.model.request.CreateAppointmentRequest;
import cl.rednorte.bff.model.response.AppointmentResponse;
import cl.rednorte.bff.model.response.WaitlistEntryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Orquestador BFF para la reasignación automática de citas.
 *
 * Reglas de negocio:
 *  - MÉDICO cancela → hora liberada → BFF busca el siguiente en lista de espera
 *    (GET /api/waitlist/specialty/{specialty}/next) → crea nueva cita al siguiente
 *    disponible en la misma especialidad → marca la entrada de waitlist como OFFERED.
 *    (Sin notificaciones en esta evaluación.)
 *
 *  - PACIENTE cancela → cita se pierde → BFF reencola al paciente al FINAL de la cola
 *    (PUT /api/waitlist/{entryId}/requeue) para que no perjudique al resto.
 *
 * Esta clase SOLO orquesta; no contiene lógica de dominio propia — esa reside en cada MS.
 */
@Service
public class ReasignacionService {

    private static final Logger log = LoggerFactory.getLogger(ReasignacionService.class);

    private final AppointmentService appointmentService;
    private final WaitlistService waitlistService;

    // ms-appointment usa inglés; ms-waitlist usa español — este mapa traduce entre ambos
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

    public ReasignacionService(AppointmentService appointmentService, WaitlistService waitlistService) {
        this.appointmentService = appointmentService;
        this.waitlistService = waitlistService;
    }

    // ─── Cancelación por médico ───────────────────────────────────────────────

    /**
     * Flujo completo cuando el médico cancela una cita:
     *  1. Cancela la cita en ms-appointment (registra cancelledBy=DOCTOR).
     *  2. Consulta el siguiente paciente en la lista de espera de la misma especialidad.
     *  3. Si existe, crea una nueva cita con el mismo médico en la siguiente franja libre.
     *  4. Marca la entrada del paciente como OFFERED en ms-waitlist.
     *
     * @param appointmentId   ID de la cita a cancelar
     * @param waitlistEntryId ID de la entrada en waitlist del siguiente paciente (puede ser null;
     *                        si es null el BFF lo resuelve consultando ms-waitlist)
     */
    public ReasignacionResult cancelByDoctor(String appointmentId, String waitlistEntryId) {
        // 1. Cancelar la cita en ms-appointment
        AppointmentResponse cancelled = appointmentService.cancelByDoctor(appointmentId);
        log.info("Cita {} cancelada por DOCTOR — especialidad={}, médico={}",
                appointmentId, cancelled.specialty(), cancelled.doctorId());

        // 2. Buscar siguiente en la cola de lista de espera para esa especialidad
        // Traduce de inglés (ms-appointment) a español (ms-waitlist)
        String waitlistSpecialty = APPT_TO_WAITLIST_SPECIALTY.getOrDefault(
                cancelled.specialty(), cancelled.specialty());
        WaitlistEntryResponse next = null;
        if (waitlistEntryId != null && !waitlistEntryId.isBlank()) {
            next = waitlistService.getById(waitlistEntryId);
        } else {
            next = waitlistService.getNextForSpecialty(waitlistSpecialty);
        }

        if (next == null) {
            log.info("No hay pacientes en lista de espera para especialidad={} — hora liberada sin reasignar",
                    cancelled.specialty());
            return new ReasignacionResult(cancelled, null, null, "HORA_LIBERADA_SIN_REASIGNAR");
        }

        // 3. Crear nueva cita para el siguiente paciente con el mismo médico
        LocalDateTime nextSlot = nextAvailableSlot(cancelled.doctorId(), cancelled.scheduledAt());
        String apptType = next.appointmentType() != null ? next.appointmentType() : "CONSULTA";

        CreateAppointmentRequest newAppt = new CreateAppointmentRequest(
                next.patientId(),
                cancelled.doctorId(),
                cancelled.specialty(),
                apptType,
                nextSlot,
                "Reasignación automática por cancelación del médico"
        );

        AppointmentResponse reassigned = appointmentService.create(newAppt);
        log.info("Nueva cita {} creada para paciente {} (siguiente en cola) — slot={}",
                reassigned.id(), next.patientId(), nextSlot);

        // 4. Marcar la entrada de waitlist como OFFERED
        WaitlistEntryResponse offered = waitlistService.markAsOffered(next.id());
        log.info("Entrada waitlist {} marcada como OFFERED para paciente {}", next.id(), next.patientId());

        return new ReasignacionResult(cancelled, reassigned, offered, "REASIGNADO");
    }

    // ─── Cancelación por paciente ─────────────────────────────────────────────

    /**
     * Flujo completo cuando el paciente cancela su cita:
     *  1. Cancela la cita en ms-appointment (registra cancelledBy=PATIENT).
     *  2. Reencola al paciente al FINAL de la cola de lista de espera (resetea requeuedAt).
     *
     * @param appointmentId   ID de la cita a cancelar
     * @param waitlistEntryId ID de la entrada en waitlist del paciente (para reencolar)
     */
    public ReasignacionResult cancelByPatient(String appointmentId, String waitlistEntryId) {
        AppointmentResponse cancelled = appointmentService.cancelByPatient(appointmentId);
        log.info("Cita {} cancelada por PACIENTE — paciente={}", appointmentId, cancelled.patientId());

        WaitlistEntryResponse requeued = null;
        if (waitlistEntryId != null && !waitlistEntryId.isBlank()) {
            requeued = waitlistService.requeueToEnd(waitlistEntryId);
            log.info("Paciente {} reubicado al final de la cola en especialidad={}",
                    cancelled.patientId(), cancelled.specialty());
        } else {
            log.warn("No se proporcionó waitlistEntryId — paciente {} no fue reubicado en cola",
                    cancelled.patientId());
        }

        return new ReasignacionResult(cancelled, null, requeued, "PACIENTE_REUBICADO_AL_FINAL");
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Calcula el siguiente slot de 30 minutos disponible para el médico.
     * Horario: Lunes–Viernes, 08:00–17:00, bloques de 30 min.
     */
    private LocalDateTime nextAvailableSlot(String doctorId, LocalDateTime fromHint) {
        List<AppointmentResponse> doctorAppts = appointmentService.getByDoctor(doctorId);

        LocalDateTime candidate = fromHint != null
                ? fromHint.plusMinutes(30)
                : LocalDateTime.now().plusMinutes(30);

        // Redondear al siguiente bloque de 30 min
        int mins = candidate.getMinute();
        if (mins > 0 && mins <= 30) {
            candidate = candidate.withMinute(30).withSecond(0).withNano(0);
        } else if (mins > 30) {
            candidate = candidate.plusHours(1).withMinute(0).withSecond(0).withNano(0);
        }

        for (int attempt = 0; attempt < 30 * 16; attempt++) {
            LocalDate day = candidate.toLocalDate();
            int dow = day.getDayOfWeek().getValue(); // 1=Lun, 7=Dom
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

    // ─── DTO resultado ────────────────────────────────────────────────────────

    public record ReasignacionResult(
            AppointmentResponse cancelledAppointment,
            AppointmentResponse newAppointment,
            WaitlistEntryResponse waitlistEntry,
            String outcome  // REASIGNADO | HORA_LIBERADA_SIN_REASIGNAR | PACIENTE_REUBICADO_AL_FINAL
    ) {}
}
