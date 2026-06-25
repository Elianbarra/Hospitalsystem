package com.hospital.msappointment.entity.enums;

/**
 * Indica quién canceló la cita.
 * DOCTOR → la hora queda disponible para reasignación automática.
 * PATIENT → el paciente va al final de la cola de lista de espera.
 */
public enum CancelledBy {
    DOCTOR,
    PATIENT
}
