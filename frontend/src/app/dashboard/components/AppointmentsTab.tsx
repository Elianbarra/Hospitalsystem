"use client";
import { useEffect } from "react";
import Alert from "@/app/components/Alert";
import { SlotPicker } from "@/app/components/SlotPicker";
import {
  SPECIALTY_LABELS,
  APPOINTMENT_STATUS_COLORS,
} from "@/features/appointments/appointments.constants";
import { useAppointments } from "@/features/appointments/useAppointments";

interface AppointmentsTabProps {
  sessionUserId: string;
  sessionRole: string;
  sessionToken: string;
  isStaff: boolean;
}

export function AppointmentsTab({
  sessionUserId,
  sessionRole,
  sessionToken,
  isStaff,
}: AppointmentsTabProps) {
  const appt = useAppointments({
    userId: sessionUserId,
    role: sessionRole,
    token: sessionToken,
  });

  useEffect(() => {
    appt.load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-lg font-bold text-gray-900">
          {sessionRole === "DOCTOR" ? "Citas asignadas" : "Mis citas médicas"}
        </h2>
        {!isStaff && (
          <button
            onClick={appt.toggleForm}
            className="bg-blue-700 text-white px-4 py-2 rounded-lg text-sm font-semibold hover:bg-blue-800 transition-colors"
          >
            {appt.showForm ? "Cancelar" : "+ Nueva cita"}
          </button>
        )}
      </div>

      {appt.success && (
        <Alert variant="success" message={appt.success} className="mb-4" />
      )}
      {appt.error && (
        <Alert variant="error" message={appt.error} className="mb-4" />
      )}

      {appt.showForm && !isStaff && (
        <div className="bg-white border border-gray-100 rounded-2xl p-6 mb-6 shadow-sm">
          <h3 className="text-sm font-bold text-gray-700 mb-4">
            Solicitar nueva cita
          </h3>
          <form onSubmit={appt.handleCreate} className="flex flex-col gap-4">
            {/* Paso 1: Especialidad */}
            <div>
              <label className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-1 block">
                1. Selecciona la especialidad
              </label>
              <select
                value={appt.specialty}
                onChange={(e) => appt.onSpecialtyChange(e.target.value)}
                className="w-full border border-gray-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">— Especialidad —</option>
                {Object.entries(SPECIALTY_LABELS).map(([k, v]) => (
                  <option key={k} value={k}>
                    {v}
                  </option>
                ))}
              </select>
              {appt.waitlistPosition !== null && (
                <p className="text-xs text-amber-600 font-semibold mt-2">
                  📋 Estás en posición #{appt.waitlistPosition} de la lista de
                  espera para esta especialidad
                </p>
              )}
              {appt.waitlistPosition === null && appt.specialty && (
                <p className="text-xs text-gray-400 mt-2">
                  No estás en lista de espera para esta especialidad
                </p>
              )}
            </div>

            {/* Paso 2: Médico */}
            {appt.specialty && (
              <div>
                <label className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-1 block">
                  2. Selecciona el médico
                </label>
                {appt.doctors.length === 0 ? (
                  <p className="text-sm text-gray-400">
                    No hay médicos disponibles para esta especialidad
                  </p>
                ) : (
                  <select
                    value={appt.doctorId}
                    onChange={(e) => appt.onDoctorChange(e.target.value)}
                    className="w-full border border-gray-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="">— Selecciona médico —</option>
                    {appt.doctors.map((d) => (
                      <option key={d.id} value={d.id}>
                        Dr. {d.firstName} {d.lastName}
                      </option>
                    ))}
                  </select>
                )}
              </div>
            )}

            {/* Paso 3: Horario */}
            {appt.doctorId && (
              <div>
                <label className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2 block">
                  3. Selecciona un horario disponible
                </label>
                {appt.slotsLoading && (
                  <p className="text-sm text-gray-400">
                    Buscando horarios disponibles...
                  </p>
                )}
                {!appt.slotsLoading && appt.availableSlots.length === 0 && (
                  <p className="text-sm text-gray-400">
                    No hay horarios disponibles en los próximos 6 meses
                  </p>
                )}
                {!appt.slotsLoading && appt.availableSlots.length > 0 && (
                  <SlotPicker
                    slots={appt.availableSlots}
                    selectedSlot={appt.selectedSlot}
                    onSelect={appt.setSelectedSlot}
                  />
                )}
                {appt.selectedSlot && (
                  <p className="text-xs text-blue-700 font-semibold mt-2">
                    ✓ Seleccionado:{" "}
                    {new Date(appt.selectedSlot).toLocaleString("es-ES", {
                      weekday: "long",
                      day: "numeric",
                      month: "long",
                      hour: "2-digit",
                      minute: "2-digit",
                    })}
                  </p>
                )}
              </div>
            )}

            {/* Tipo y notas */}
            {appt.doctorId && (
              <>
                <div>
                  <label className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2 block">
                    Tipo de atención
                  </label>
                  <div className="flex gap-2">
                    {(["CONSULTA", "CIRUGIA"] as const).map((t) => (
                      <button
                        key={t}
                        type="button"
                        onClick={() => appt.setType(t)}
                        className={`flex-1 py-2.5 px-4 rounded-xl text-sm font-semibold border-2 transition-all ${
                          appt.type === t
                            ? "bg-blue-700 text-white border-blue-700"
                            : "border-gray-200 text-gray-400 hover:border-gray-300"
                        }`}
                      >
                        {t === "CONSULTA" ? "🩺 Consulta" : "🔬 Cirugía"}
                      </button>
                    ))}
                  </div>
                </div>

                <textarea
                  placeholder="Notas adicionales (opcional)"
                  value={appt.notes}
                  onChange={(e) => appt.setNotes(e.target.value)}
                  rows={2}
                  className="border border-gray-200 rounded-xl px-4 py-3 text-sm resize-none focus:outline-none focus:ring-2 focus:ring-blue-500"
                />

                <button
                  type="submit"
                  disabled={appt.submitting || !appt.selectedSlot}
                  className="w-full bg-blue-700 text-white py-3.5 rounded-xl text-sm font-semibold hover:bg-blue-800 disabled:bg-gray-200 disabled:cursor-not-allowed transition-colors"
                >
                  {appt.submitting ? "Solicitando..." : "Confirmar cita"}
                </button>
              </>
            )}
          </form>
        </div>
      )}

      {appt.loading && (
        <p className="text-sm text-gray-400">Cargando citas...</p>
      )}
      {!appt.loading && appt.appointments.length === 0 && (
        <div className="bg-white rounded-2xl p-12 text-center text-gray-400 text-sm border border-gray-100">
          No hay citas registradas
        </div>
      )}

      <div className="flex flex-col gap-3">
        {appt.appointments.map((a) => {
          const isCancelled = a.status === "CANCELLED";
          return (
            <div
              key={a.id}
              className="bg-white border border-gray-100 rounded-2xl p-5 shadow-sm flex items-start justify-between gap-4"
            >
              <div className="flex-1">
                <div className="flex items-center gap-2 mb-1">
                  <span
                    className={`text-xs font-bold px-2 py-0.5 rounded-full ${
                      APPOINTMENT_STATUS_COLORS[a.status] ??
                      "bg-gray-100 text-gray-600"
                    }`}
                  >
                    {a.status}
                  </span>
                  <span className="text-xs bg-purple-50 text-purple-700 font-semibold px-2 py-0.5 rounded-full">
                    {a.appointmentType === "CIRUGIA" ? "🔬 Cirugía" : "🩺 Consulta"}
                  </span>
                  <span className="text-xs text-gray-400 font-medium">
                    {SPECIALTY_LABELS[a.specialty] ?? a.specialty}
                  </span>
                </div>
                <p className="text-sm font-semibold text-gray-800">
                  {new Date(a.scheduledAt).toLocaleString("es-ES", {
                    weekday: "short",
                    day: "numeric",
                    month: "long",
                    hour: "2-digit",
                    minute: "2-digit",
                  })}
                </p>
                {a.cancelledBy && (
                  <p className="text-xs text-red-400 mt-1">
                    Cancelada por: {a.cancelledBy}
                  </p>
                )}
                {a.notes && (
                  <p className="text-xs text-gray-400 mt-1">{a.notes}</p>
                )}
              </div>
              {!isCancelled && (
                <button
                  onClick={() => appt.handleCancel(a)}
                  className="text-xs text-red-500 hover:text-red-700 font-semibold shrink-0 transition-colors"
                >
                  {sessionRole === "DOCTOR" ? "Cancelar y reasignar" : "Cancelar"}
                </button>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}
