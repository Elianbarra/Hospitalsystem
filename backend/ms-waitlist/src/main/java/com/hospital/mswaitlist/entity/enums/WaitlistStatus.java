package com.hospital.mswaitlist.entity.enums;

public enum WaitlistStatus {
    /** Paciente en espera, sin cupo disponible aún */
    WAITING,
    /** Se notificó al paciente que hay disponibilidad */
    NOTIFIED,
    /** Cupo asignado — paciente sale de la lista */
    ASSIGNED,
    /** Entrada cancelada por el paciente o administrativo */
    CANCELLED
}
