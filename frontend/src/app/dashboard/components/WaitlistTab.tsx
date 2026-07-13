"use client";
import { useEffect } from "react";
import Alert from "@/app/components/Alert";
import {
  SPECIALTY_LABELS,
  PRIORITY_COLORS,
  WAITLIST_STATUS_COLORS,
} from "@/features/appointments/appointments.constants";
import { useWaitlist } from "@/features/waitlist/useWaitlist";

interface WaitlistTabProps {
  sessionUserId: string;
  sessionRole: string;
  sessionToken: string;
  isStaff: boolean;
}

export function WaitlistTab({
  sessionUserId,
  sessionRole,
  sessionToken,
  isStaff,
}: WaitlistTabProps) {
  const wl = useWaitlist({
    userId: sessionUserId,
    role: sessionRole,
    token: sessionToken,
  });

  useEffect(() => {
    wl.load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const isPrivileged = sessionRole === "DOCTOR" || sessionRole === "ADMIN";

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-lg font-bold text-gray-900">
          {isPrivileged
            ? "Lista de espera (todas las especialidades)"
            : "Mi lista de espera"}
        </h2>
        {!isStaff && (
          <button
            onClick={wl.toggleForm}
            className="bg-green-700 text-white px-4 py-2 rounded-lg text-sm font-semibold hover:bg-green-800 transition-colors"
          >
            {wl.showForm ? "Cancelar" : "+ Unirse a lista de espera"}
          </button>
        )}
      </div>

      {wl.success && (
        <Alert variant="success" message={wl.success} className="mb-4" />
      )}
      {wl.error && (
        <Alert variant="error" message={wl.error} className="mb-4" />
      )}

      {wl.showForm && !isStaff && (
        <div className="bg-white border border-gray-100 rounded-2xl p-6 mb-6 shadow-sm">
          <h3 className="text-sm font-bold text-gray-700 mb-4">
            Inscribirse en lista de espera
          </h3>
          <form onSubmit={wl.handleJoin} className="flex flex-col gap-4">
            <div>
              <label className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-1 block">
                Especialidad
              </label>
              <select
                value={wl.specialty}
                onChange={(e) => wl.setSpecialty(e.target.value)}
                required
                className="w-full border border-gray-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-green-500"
              >
                <option value="">— Selecciona especialidad —</option>
                {Object.entries(SPECIALTY_LABELS).map(([k, v]) => (
                  <option key={k} value={k}>
                    {v}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2 block">
                Tipo de atención
              </label>
              <div className="flex gap-2">
                {(["CONSULTA", "CIRUGIA"] as const).map((t) => (
                  <button
                    key={t}
                    type="button"
                    onClick={() => wl.setType(t)}
                    className={`flex-1 py-2.5 px-4 rounded-xl text-sm font-semibold border-2 transition-all ${
                      wl.type === t
                        ? "bg-green-700 text-white border-green-700"
                        : "border-gray-200 text-gray-400 hover:border-gray-300"
                    }`}
                  >
                    {t === "CONSULTA" ? "🩺 Consulta" : "🔬 Cirugía"}
                  </button>
                ))}
              </div>
            </div>

            <textarea
              placeholder="Motivo / notas (opcional)"
              value={wl.notes}
              onChange={(e) => wl.setNotes(e.target.value)}
              rows={2}
              className="border border-gray-200 rounded-xl px-4 py-3 text-sm resize-none focus:outline-none focus:ring-2 focus:ring-green-500"
            />

            <button
              type="submit"
              disabled={wl.submitting || !wl.specialty}
              className="w-full bg-green-700 text-white py-3.5 rounded-xl text-sm font-semibold hover:bg-green-800 disabled:bg-gray-200 disabled:cursor-not-allowed transition-colors"
            >
              {wl.submitting ? "Inscribiendo..." : "Confirmar inscripción"}
            </button>
          </form>
        </div>
      )}

      {wl.loading && (
        <p className="text-sm text-gray-400">Cargando lista de espera...</p>
      )}
      {!wl.loading && wl.waitlist.length === 0 && (
        <div className="bg-white rounded-2xl p-12 text-center text-gray-400 text-sm border border-gray-100">
          No hay entradas en lista de espera
        </div>
      )}

      <div className="flex flex-col gap-3">
        {wl.waitlist.map((entry, idx) => (
          <div
            key={entry.id}
            className="bg-white border border-gray-100 rounded-2xl p-5 shadow-sm"
          >
            <div className="flex items-start justify-between gap-4">
              <div className="flex-1">
                <div className="flex items-center gap-2 flex-wrap mb-1">
                  {isPrivileged && (
                    <span className="text-xs font-bold text-gray-300">
                      #{idx + 1}
                    </span>
                  )}
                  <span
                    className={`text-xs font-bold px-2 py-0.5 rounded-full ${
                      PRIORITY_COLORS[entry.priority] ??
                      "bg-gray-100 text-gray-600"
                    }`}
                  >
                    {entry.priority}
                  </span>
                  <span
                    className={`text-xs font-semibold px-2 py-0.5 rounded-full ${
                      WAITLIST_STATUS_COLORS[entry.status] ??
                      "bg-gray-100 text-gray-600"
                    }`}
                  >
                    {entry.status}
                  </span>
                  <span className="text-xs font-semibold bg-purple-50 text-purple-700 px-2 py-0.5 rounded-full">
                    {entry.appointmentType === "CIRUGIA"
                      ? "🔬 Cirugía"
                      : "🩺 Consulta"}
                  </span>
                  {entry.vitalRisk && (
                    <span className="text-xs font-bold bg-red-600 text-white px-2 py-0.5 rounded-full animate-pulse">
                      ⚠️ RIESGO VITAL
                    </span>
                  )}
                  <span className="text-xs text-gray-400">
                    {SPECIALTY_LABELS[entry.specialty] ?? entry.specialty}
                  </span>
                </div>

                <p className="text-xs text-gray-400 mt-1">
                  Inscrito:{" "}
                  {new Date(entry.createdAt).toLocaleDateString("es-ES")}
                  {entry.requeuedAt &&
                    entry.requeuedAt !== entry.createdAt && (
                      <>
                        {" "}
                        · Reubicado:{" "}
                        {new Date(entry.requeuedAt).toLocaleString("es-ES")}
                      </>
                    )}
                </p>
                {entry.notes && (
                  <p className="text-xs text-gray-500 mt-1">{entry.notes}</p>
                )}

                {isPrivileged && entry.status !== "CANCELLED" && (
                  <div className="flex items-center gap-3 mt-3">
                    <span className="text-xs font-semibold text-gray-500">
                      Riesgo vital:
                    </span>
                    <button
                      onClick={() =>
                        wl.handleUpdateVitalRisk(entry.id, !entry.vitalRisk)
                      }
                      className={`relative inline-flex h-5 w-9 items-center rounded-full transition-colors ${
                        entry.vitalRisk ? "bg-red-500" : "bg-gray-200"
                      }`}
                    >
                      <span
                        className={`inline-block h-4 w-4 transform rounded-full bg-white shadow transition-transform ${
                          entry.vitalRisk ? "translate-x-4" : "translate-x-0.5"
                        }`}
                      />
                    </button>
                    <span className="text-xs text-gray-400">
                      {entry.vitalRisk
                        ? "Activo — sube al tope de la cola"
                        : "Inactivo"}
                    </span>
                  </div>
                )}
              </div>

              {entry.status !== "CANCELLED" && (
                <button
                  onClick={() => wl.handleCancel(entry.id)}
                  className="text-xs text-red-500 hover:text-red-700 font-semibold shrink-0 transition-colors"
                >
                  Cancelar
                </button>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
