"use client";
import { useEffect, useState, useCallback } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { HospitalLogo } from "@/app/components/HospitalLogo";
import { Alert } from "@/app/components/Alert";
import { ROLE_LABELS, DOC_LABELS } from "@/features/users/users.constants";
import { getSession, clearSession } from "@/lib/session";
import { BFF_URL } from "@/lib/bff";

// ── Types ──────────────────────────────────────────────────────────────────────
interface UserProfile {
  id: string; firstName: string; lastName: string; email: string;
  phone: string; documentType: string; documentNumber: string;
  role: string; isActive: boolean; createdAt: string;
}
interface Appointment {
  id: string; patientId: string; doctorId: string; specialty: string;
  scheduledAt: string; status: string; notes: string; createdAt: string;
}
interface WaitlistEntry {
  id: string; patientId: string; specialty: string;
  priority: string; // NORMAL | URGENTE | CRITICO
  status: string;   // WAITING | NOTIFIED | ASSIGNED | CANCELLED
  notes: string; createdAt: string; updatedAt: string;
}

// ── Constants ──────────────────────────────────────────────────────────────────
const APPOINTMENT_SPECIALTIES = [
  "GENERAL","CARDIOLOGY","NEUROLOGY","PEDIATRICS","ORTHOPEDICS",
  "DERMATOLOGY","GYNECOLOGY","OPHTHALMOLOGY","PSYCHIATRY",
  "TRAUMATOLOGY","INTERNAL_MEDICINE","EMERGENCY",
];
const WAITLIST_SPECIALTIES = [
  "MEDICINA_GENERAL","PEDIATRIA","CARDIOLOGIA","TRAUMATOLOGIA",
  "NEUROLOGIA","GINECOLOGIA","OFTALMOLOGIA","DERMATOLOGIA",
  "PSIQUIATRIA","ONCOLOGIA",
];
const SPECIALTY_LABELS: Record<string, string> = {
  GENERAL: "General", CARDIOLOGY: "Cardiología", NEUROLOGY: "Neurología",
  PEDIATRICS: "Pediatría", ORTHOPEDICS: "Ortopedia", DERMATOLOGY: "Dermatología",
  GYNECOLOGY: "Ginecología", OPHTHALMOLOGY: "Oftalmología", PSYCHIATRY: "Psiquiatría",
  TRAUMATOLOGY: "Traumatología", INTERNAL_MEDICINE: "Medicina Interna", EMERGENCY: "Urgencias",
  MEDICINA_GENERAL: "Medicina General", PEDIATRIA: "Pediatría", CARDIOLOGIA: "Cardiología",
  TRAUMATOLOGIA: "Traumatología", NEUROLOGIA: "Neurología", GINECOLOGIA: "Ginecología",
  OFTALMOLOGIA: "Oftalmología", DERMATOLOGIA: "Dermatología", PSIQUIATRIA: "Psiquiatría",
  ONCOLOGIA: "Oncología",
};
const STATUS_COLORS: Record<string, string> = {
  PENDING: "bg-yellow-100 text-yellow-700", CONFIRMED: "bg-blue-100 text-blue-700",
  COMPLETED: "bg-green-100 text-green-700", CANCELLED: "bg-red-100 text-red-700",
  WAITING: "bg-yellow-100 text-yellow-700", NOTIFIED: "bg-blue-100 text-blue-700",
  ASSIGNED: "bg-green-100 text-green-700",
};
const STATUS_LABELS: Record<string, string> = {
  PENDING: "Pendiente", CONFIRMED: "Confirmada", COMPLETED: "Completada",
  CANCELLED: "Cancelada", WAITING: "En espera", NOTIFIED: "Notificado", ASSIGNED: "Asignado",
};
const PRIORITY_COLORS: Record<string, string> = {
  NORMAL: "bg-gray-100 text-gray-600",
  URGENTE: "bg-yellow-100 text-yellow-700",
  CRITICO: "bg-red-100 text-red-700",
};
const PRIORITY_LABELS: Record<string, string> = {
  NORMAL: "Normal", URGENTE: "Urgente", CRITICO: "Crítico",
};

// ── Helpers ────────────────────────────────────────────────────────────────────
const fmt = (d: string) => d ? new Date(d).toLocaleDateString("es-ES", { year: "numeric", month: "short", day: "numeric" }) : "—";
const fmtDT = (d: string) => d ? new Date(d).toLocaleString("es-ES", { year: "numeric", month: "short", day: "numeric", hour: "2-digit", minute: "2-digit" }) : "—";
const inputCls = "w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500";
const btnPrimary = "bg-blue-700 text-white px-4 py-2 rounded-lg text-sm font-semibold hover:bg-blue-800 disabled:bg-gray-300 transition-colors";
const btnDanger = "bg-red-50 text-red-600 px-3 py-1.5 rounded-lg text-xs font-semibold hover:bg-red-100 transition-colors";
const btnSuccess = "bg-green-50 text-green-700 px-3 py-1.5 rounded-lg text-xs font-semibold hover:bg-green-100 transition-colors";

// ── Modal wrapper ──────────────────────────────────────────────────────────────
function Modal({ title, onClose, children }: { title: string; onClose: () => void; children: React.ReactNode }) {
  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-md p-6">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-base font-bold text-gray-800">{title}</h3>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600 text-xl leading-none">×</button>
        </div>
        {children}
      </div>
    </div>
  );
}

// ── Main component ─────────────────────────────────────────────────────────────
export default function Dashboard() {
  const [user, setUser]           = useState<UserProfile | null>(null);
  const [doctors, setDoctors]     = useState<UserProfile[]>([]);
  const [appointments, setAppts]  = useState<Appointment[]>([]);
  const [waitlist, setWaitlist]   = useState<WaitlistEntry[]>([]);
  const [allWaitlist, setAllWaitlist] = useState<WaitlistEntry[]>([]);
  const [users, setUsers]         = useState<UserProfile[]>([]);
  const [tab, setTab]             = useState("perfil");
  const [loading, setLoading]     = useState(true);
  const [error, setError]         = useState("");
  const [toast, setToast]         = useState("");
  const [sessionRole, setSessionRole] = useState("");
  const [sessionEmail, setSessionEmail] = useState("");
  const [token, setToken]         = useState<string | null>(null);
  const [userId, setUserId]       = useState<string | null>(null);

  // Modals
  const [showApptForm, setShowApptForm]     = useState(false);
  const [showWaitForm, setShowWaitForm]     = useState(false);

  // New appointment form state
  const [apptForm, setApptForm] = useState({ doctorId: "", specialty: APPOINTMENT_SPECIALTIES[0], scheduledAt: "", notes: "" });
  const [apptLoading, setApptLoading] = useState(false);
  const [apptError, setApptError] = useState("");

  // New waitlist form state (patient only sends specialty + notes; no priority)
  const [waitForm, setWaitForm] = useState({ specialty: WAITLIST_SPECIALTIES[0], notes: "" });
  const [waitLoading, setWaitLoading] = useState(false);
  const [waitError, setWaitError] = useState("");

  // Priority update state (doctor/admin)
  const [updatingPriority, setUpdatingPriority] = useState<string | null>(null);

  const router = useRouter();
  const showToast = (msg: string) => { setToast(msg); setTimeout(() => setToast(""), 3000); };

  const fetchAppts = useCallback((role: string, uid: string, tok: string) => {
    const url = role === "PATIENT"
      ? `${BFF_URL}/api/appointments/patient/${uid}`
      : role === "DOCTOR"
      ? `${BFF_URL}/api/appointments/doctor/${uid}`
      : `${BFF_URL}/api/appointments`;
    fetch(url, { headers: { Authorization: `Bearer ${tok}` } })
      .then(r => r.ok ? r.json() : []).then(setAppts).catch(() => {});
  }, []);

  const fetchWaitlist = useCallback((uid: string, tok: string) => {
    fetch(`${BFF_URL}/api/waitlist/patient/${uid}`, { headers: { Authorization: `Bearer ${tok}` } })
      .then(r => r.ok ? r.json() : []).then(setWaitlist).catch(() => {});
  }, []);

  const fetchAllWaitlist = useCallback((tok: string) => {
    fetch(`${BFF_URL}/api/waitlist`, { headers: { Authorization: `Bearer ${tok}` } })
      .then(r => r.ok ? r.json() : []).then(setAllWaitlist).catch(() => {});
  }, []);

  useEffect(() => {
    const { token: tok, userId: uid, email, role } = getSession();
    if (!tok || !uid) { router.push("/"); return; }

    setToken(tok); setUserId(uid);
    setSessionEmail(email); setSessionRole(role);

    // Profile
    fetch(`${BFF_URL}/api/users/${uid}`, { headers: { Authorization: `Bearer ${tok}` } })
      .then(r => { if (!r.ok) throw new Error("No se pudo cargar el perfil"); return r.json(); })
      .then(setUser).catch((e: Error) => setError(e.message)).finally(() => setLoading(false));

    // All users (doctor selector + admin panel)
    fetch(`${BFF_URL}/api/users`, { headers: { Authorization: `Bearer ${tok}` } })
      .then(r => r.ok ? r.json() : [])
      .then((data: UserProfile[]) => {
        setUsers(data);
        setDoctors(data.filter(u => u.role === "DOCTOR"));
      }).catch(() => {});

    fetchAppts(role, uid, tok);
    if (role === "PATIENT") fetchWaitlist(uid, tok);
    if (role === "DOCTOR" || role === "ADMIN") fetchAllWaitlist(tok);
  }, [router, fetchAppts, fetchWaitlist, fetchAllWaitlist]);

  // ── Actions ────────────────────────────────────────────────────────────────
  async function cancelAppointment(id: string) {
    if (!token) return;
    await fetch(`${BFF_URL}/api/appointments/${id}/cancel`, {
      method: "PUT", headers: { Authorization: `Bearer ${token}` },
    });
    fetchAppts(sessionRole, userId!, token);
    showToast("Cita cancelada");
  }

  async function updateAppointmentStatus(id: string, status: string) {
    if (!token) return;
    await fetch(`${BFF_URL}/api/appointments/${id}`, {
      method: "PUT",
      headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
      body: JSON.stringify({ status }),
    });
    fetchAppts(sessionRole, userId!, token);
    showToast("Estado actualizado");
  }

  async function cancelWaitlistEntry(id: string) {
    if (!token) return;
    await fetch(`${BFF_URL}/api/waitlist/${id}/cancel`, {
      method: "PUT", headers: { Authorization: `Bearer ${token}` },
    });
    if (sessionRole === "PATIENT") fetchWaitlist(userId!, token);
    else fetchAllWaitlist(token);
    showToast("Entrada cancelada");
  }

  async function updateWaitlistPriority(id: string, priority: string) {
    if (!token) return;
    setUpdatingPriority(id);
    try {
      await fetch(`${BFF_URL}/api/waitlist/${id}`, {
        method: "PUT",
        headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
        body: JSON.stringify({ priority }),
      });
      fetchAllWaitlist(token);
      showToast(`Prioridad actualizada a ${PRIORITY_LABELS[priority]}`);
    } finally {
      setUpdatingPriority(null);
    }
  }

  async function submitAppointment(e: React.FormEvent) {
    e.preventDefault();
    if (!token || !userId) return;
    setApptLoading(true); setApptError("");
    try {
      const res = await fetch(`${BFF_URL}/api/appointments`, {
        method: "POST",
        headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
        body: JSON.stringify({ ...apptForm, patientId: userId }),
      });
      if (!res.ok) {
        const d = await res.json();
        setApptError(d.message ?? "Error al crear la cita");
        return;
      }
      setShowApptForm(false);
      setApptForm({ doctorId: "", specialty: APPOINTMENT_SPECIALTIES[0], scheduledAt: "", notes: "" });
      fetchAppts(sessionRole, userId, token);
      showToast("Cita creada correctamente");
    } catch { setApptError("Error de conexión"); }
    finally { setApptLoading(false); }
  }

  async function submitWaitlist(e: React.FormEvent) {
    e.preventDefault();
    if (!token || !userId) return;
    setWaitLoading(true); setWaitError("");
    try {
      const res = await fetch(`${BFF_URL}/api/waitlist`, {
        method: "POST",
        headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
        body: JSON.stringify({ ...waitForm, patientId: userId }),
      });
      if (!res.ok) {
        const d = await res.json();
        setWaitError(d.message ?? "Error al unirse a la lista");
        return;
      }
      setShowWaitForm(false);
      setWaitForm({ specialty: WAITLIST_SPECIALTIES[0], notes: "" });
      fetchWaitlist(userId, token);
      showToast("Agregado a lista de espera");
    } catch { setWaitError("Error de conexión"); }
    finally { setWaitLoading(false); }
  }

  // ── Tabs ───────────────────────────────────────────────────────────────────
  const initials = user ? `${user.firstName[0]}${user.lastName[0]}`.toUpperCase() : "??";
  const tabs = [
    { id: "perfil", label: "Mi perfil" },
    ...(sessionRole === "PATIENT" ? [
      { id: "citas", label: `Mis citas (${appointments.length})` },
      { id: "espera", label: `Lista de espera (${waitlist.length})` },
    ] : []),
    ...(sessionRole === "DOCTOR" ? [
      { id: "citas", label: `Mis citas (${appointments.length})` },
      { id: "espera", label: `Lista de espera (${allWaitlist.length})` },
    ] : []),
    ...(sessionRole === "ADMIN" ? [
      { id: "citas", label: `Citas (${appointments.length})` },
      { id: "espera", label: `Lista de espera (${allWaitlist.length})` },
      { id: "usuarios", label: `Usuarios (${users.length})` },
    ] : []),
  ];

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      {/* Toast */}
      {toast && (
        <div className="fixed top-4 right-4 bg-green-600 text-white text-sm font-semibold px-5 py-3 rounded-xl shadow-lg z-50">
          {toast}
        </div>
      )}

      {/* Modals */}
      {showApptForm && (
        <Modal title="Nueva cita médica" onClose={() => setShowApptForm(false)}>
          <form onSubmit={submitAppointment} className="flex flex-col gap-3">
            {apptError && <Alert variant="error" message={apptError} />}
            <div>
              <label className="text-xs font-semibold text-gray-500 mb-1 block">Especialidad</label>
              <select className={inputCls} value={apptForm.specialty}
                onChange={e => setApptForm(p => ({ ...p, specialty: e.target.value }))}>
                {APPOINTMENT_SPECIALTIES.map(s => <option key={s} value={s}>{SPECIALTY_LABELS[s] ?? s}</option>)}
              </select>
            </div>
            <div>
              <label className="text-xs font-semibold text-gray-500 mb-1 block">Médico</label>
              <select className={inputCls} value={apptForm.doctorId} required
                onChange={e => setApptForm(p => ({ ...p, doctorId: e.target.value }))}>
                <option value="">Seleccionar médico...</option>
                {doctors.map(d => <option key={d.id} value={d.id}>{d.firstName} {d.lastName}</option>)}
              </select>
            </div>
            <div>
              <label className="text-xs font-semibold text-gray-500 mb-1 block">Fecha y hora</label>
              <input type="datetime-local" className={inputCls} required value={apptForm.scheduledAt}
                onChange={e => setApptForm(p => ({ ...p, scheduledAt: e.target.value }))} />
            </div>
            <div>
              <label className="text-xs font-semibold text-gray-500 mb-1 block">Notas (opcional)</label>
              <textarea className={inputCls} rows={2} value={apptForm.notes}
                onChange={e => setApptForm(p => ({ ...p, notes: e.target.value }))} />
            </div>
            <button type="submit" disabled={apptLoading} className={btnPrimary}>
              {apptLoading ? "Agendando..." : "Agendar cita"}
            </button>
          </form>
        </Modal>
      )}

      {showWaitForm && (
        <Modal title="Unirse a lista de espera" onClose={() => setShowWaitForm(false)}>
          <form onSubmit={submitWaitlist} className="flex flex-col gap-3">
            {waitError && <Alert variant="error" message={waitError} />}
            <div>
              <label className="text-xs font-semibold text-gray-500 mb-1 block">Especialidad</label>
              <select className={inputCls} value={waitForm.specialty}
                onChange={e => setWaitForm(p => ({ ...p, specialty: e.target.value }))}>
                {WAITLIST_SPECIALTIES.map(s => <option key={s} value={s}>{SPECIALTY_LABELS[s] ?? s}</option>)}
              </select>
            </div>
            <div>
              <label className="text-xs font-semibold text-gray-500 mb-1 block">Motivo (opcional)</label>
              <textarea className={inputCls} rows={2} value={waitForm.notes}
                onChange={e => setWaitForm(p => ({ ...p, notes: e.target.value }))} />
            </div>
            <p className="text-xs text-gray-400">La prioridad es asignada por el médico tratante.</p>
            <button type="submit" disabled={waitLoading} className={btnPrimary}>
              {waitLoading ? "Registrando..." : "Unirse a lista de espera"}
            </button>
          </form>
        </Modal>
      )}

      {/* Nav */}
      <nav className="bg-white border-b border-gray-100 px-8 py-4 flex items-center justify-between shadow-sm">
        <HospitalLogo subtitle="Portal del Paciente" />
        <div className="flex items-center gap-4">
          <span className="text-sm text-gray-500">{sessionEmail}</span>
          {sessionRole === "ADMIN" && (
            <Link href="/register/staff"
              className="bg-gray-900 text-white px-4 py-2 rounded-lg text-sm font-semibold hover:bg-gray-700 transition-colors">
              + Registrar personal
            </Link>
          )}
          <button onClick={() => { clearSession(); router.push("/"); }}
            className="text-sm text-red-500 hover:text-red-700 font-medium transition-colors">
            Cerrar sesión
          </button>
        </div>
      </nav>

      <div className="max-w-4xl mx-auto px-6 py-10 w-full">
        {error && <Alert variant="error" message={error} className="mb-6" />}

        {/* Tabs */}
        <div className="flex gap-1 mb-8 border-b border-gray-200">
          {tabs.map(t => (
            <button key={t.id} onClick={() => setTab(t.id)}
              className={`px-5 py-2.5 text-sm font-semibold rounded-t-lg transition-colors ${
                tab === t.id
                  ? "bg-white border border-b-white border-gray-200 text-blue-700 -mb-px"
                  : "text-gray-500 hover:text-gray-700"
              }`}>
              {t.label}
            </button>
          ))}
        </div>

        {/* ── Perfil ─────────────────────────────────────────────────────── */}
        {tab === "perfil" && (
          <>
            {loading && <p className="text-center text-gray-400 text-sm py-12">Cargando perfil...</p>}
            {user && (
              <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
                <div className="bg-gradient-to-r from-blue-700 to-blue-600 px-8 py-8 flex items-center gap-5">
                  <div className="w-16 h-16 bg-white/20 rounded-full flex items-center justify-center text-white text-2xl font-black">
                    {initials}
                  </div>
                  <div>
                    <h2 className="text-xl font-bold text-white">{user.firstName} {user.lastName}</h2>
                    <span className="inline-block bg-white/20 text-white text-xs font-semibold px-3 py-1 rounded-full mt-1.5">
                      {ROLE_LABELS[user.role] ?? user.role}
                    </span>
                  </div>
                  <div className="ml-auto">
                    <span className={`inline-flex items-center gap-1.5 text-xs font-semibold px-3 py-1.5 rounded-full ${
                      user.isActive ? "bg-green-400/20 text-green-100" : "bg-red-400/20 text-red-100"
                    }`}>
                      <span className={`w-1.5 h-1.5 rounded-full ${user.isActive ? "bg-green-300" : "bg-red-300"}`} />
                      {user.isActive ? "Activo" : "Inactivo"}
                    </span>
                  </div>
                </div>
                <dl className="grid grid-cols-2 gap-px bg-gray-100">
                  {([
                    ["Correo electrónico", user.email],
                    ["Teléfono", user.phone],
                    ["Tipo de documento", DOC_LABELS[user.documentType as keyof typeof DOC_LABELS] ?? user.documentType],
                    ["Número de documento", user.documentNumber],
                    ["Miembro desde", fmt(user.createdAt)],
                    ["ID de usuario", user.id],
                  ] as [string, string][]).map(([label, value]) => (
                    <div key={label} className="bg-white px-8 py-5">
                      <dt className="text-xs font-bold text-gray-400 uppercase tracking-wider mb-1">{label}</dt>
                      <dd className="text-sm font-medium text-gray-800 truncate">{value}</dd>
                    </div>
                  ))}
                </dl>
              </div>
            )}
          </>
        )}

        {/* ── Citas ──────────────────────────────────────────────────────── */}
        {tab === "citas" && (
          <div>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-bold text-gray-800">
                {sessionRole === "PATIENT" ? "Mis citas" : sessionRole === "DOCTOR" ? "Mis citas" : "Todas las citas"}
              </h2>
              {sessionRole === "PATIENT" && (
                <button onClick={() => setShowApptForm(true)} className={btnPrimary}>
                  + Nueva cita
                </button>
              )}
            </div>

            {appointments.length === 0 ? (
              <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-12 text-center text-gray-400 text-sm">
                No hay citas registradas
              </div>
            ) : (
              <div className="space-y-3">
                {appointments.map(a => (
                  <div key={a.id} className="bg-white rounded-xl border border-gray-100 shadow-sm px-6 py-4">
                    <div className="flex items-start justify-between gap-4">
                      <div className="flex flex-col gap-1 flex-1">
                        <span className="text-sm font-bold text-gray-800">
                          {SPECIALTY_LABELS[a.specialty] ?? a.specialty}
                        </span>
                        <span className="text-xs text-gray-500">{fmtDT(a.scheduledAt)}</span>
                        {a.notes && <span className="text-xs text-gray-400 italic mt-1">{a.notes}</span>}
                        <span className="text-xs text-gray-300">ID médico: {a.doctorId}</span>
                      </div>
                      <div className="flex flex-col items-end gap-2">
                        <span className={`text-xs font-semibold px-3 py-1 rounded-full ${STATUS_COLORS[a.status] ?? "bg-gray-100 text-gray-600"}`}>
                          {STATUS_LABELS[a.status] ?? a.status}
                        </span>
                        <div className="flex gap-2">
                          {sessionRole === "DOCTOR" && a.status === "PENDING" && (
                            <button onClick={() => updateAppointmentStatus(a.id, "CONFIRMED")} className={btnSuccess}>
                              Confirmar
                            </button>
                          )}
                          {sessionRole === "DOCTOR" && a.status === "CONFIRMED" && (
                            <button onClick={() => updateAppointmentStatus(a.id, "COMPLETED")} className={btnSuccess}>
                              Completar
                            </button>
                          )}
                          {a.status !== "CANCELLED" && a.status !== "COMPLETED" && (
                            <button onClick={() => cancelAppointment(a.id)} className={btnDanger}>
                              Cancelar
                            </button>
                          )}
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* ── Lista de espera ─────────────────────────────────────────────── */}
        {tab === "espera" && sessionRole === "PATIENT" && (
          <div>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-bold text-gray-800">Mi lista de espera</h2>
              <button onClick={() => setShowWaitForm(true)} className={btnPrimary}>
                + Unirse a lista de espera
              </button>
            </div>

            {waitlist.length === 0 ? (
              <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-12 text-center text-gray-400 text-sm">
                No estás en ninguna lista de espera
              </div>
            ) : (
              <div className="space-y-3">
                {waitlist.map(w => (
                  <div key={w.id} className="bg-white rounded-xl border border-gray-100 shadow-sm px-6 py-4">
                    <div className="flex items-start justify-between gap-4">
                      <div className="flex flex-col gap-1">
                        <span className="text-sm font-bold text-gray-800">
                          {SPECIALTY_LABELS[w.specialty] ?? w.specialty}
                        </span>
                        {w.notes && <span className="text-xs text-gray-500">{w.notes}</span>}
                        <span className="text-xs text-gray-400 mt-1">Solicitado: {fmt(w.createdAt)}</span>
                      </div>
                      <div className="flex flex-col items-end gap-2">
                        <div className="flex gap-2">
                          <span className={`text-xs font-semibold px-3 py-1 rounded-full ${PRIORITY_COLORS[w.priority] ?? "bg-gray-100 text-gray-600"}`}>
                            {PRIORITY_LABELS[w.priority] ?? w.priority}
                          </span>
                          <span className={`text-xs font-semibold px-3 py-1 rounded-full ${STATUS_COLORS[w.status] ?? "bg-gray-100 text-gray-600"}`}>
                            {STATUS_LABELS[w.status] ?? w.status}
                          </span>
                        </div>
                        {w.status === "WAITING" && (
                          <button onClick={() => cancelWaitlistEntry(w.id)} className={btnDanger}>
                            Cancelar
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* ── Lista de espera (DOCTOR / ADMIN) ────────────────────────────── */}
        {tab === "espera" && (sessionRole === "DOCTOR" || sessionRole === "ADMIN") && (
          <div>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-bold text-gray-800">Lista de espera</h2>
              <button onClick={() => fetchAllWaitlist(token!)}
                className="text-xs text-blue-600 hover:text-blue-800 font-semibold border border-blue-200 px-3 py-1.5 rounded-lg transition-colors">
                Actualizar
              </button>
            </div>

            {allWaitlist.length === 0 ? (
              <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-12 text-center text-gray-400 text-sm">
                No hay entradas en lista de espera
              </div>
            ) : (
              <div className="space-y-3">
                {allWaitlist.map(w => (
                  <div key={w.id} className="bg-white rounded-xl border border-gray-100 shadow-sm px-6 py-4">
                    <div className="flex items-start justify-between gap-4">
                      <div className="flex flex-col gap-1 flex-1">
                        <span className="text-sm font-bold text-gray-800">
                          {SPECIALTY_LABELS[w.specialty] ?? w.specialty}
                        </span>
                        <span className="text-xs text-gray-400">Paciente ID: {w.patientId}</span>
                        {w.notes && <span className="text-xs text-gray-500 italic">{w.notes}</span>}
                        <span className="text-xs text-gray-300">Solicitado: {fmt(w.createdAt)}</span>
                      </div>
                      <div className="flex flex-col items-end gap-2">
                        <span className={`text-xs font-semibold px-3 py-1 rounded-full ${STATUS_COLORS[w.status] ?? "bg-gray-100 text-gray-600"}`}>
                          {STATUS_LABELS[w.status] ?? w.status}
                        </span>
                        {/* Priority selector */}
                        {w.status !== "CANCELLED" && w.status !== "ASSIGNED" && (
                          <div className="flex items-center gap-2">
                            <select
                              value={w.priority}
                              disabled={updatingPriority === w.id}
                              onChange={e => updateWaitlistPriority(w.id, e.target.value)}
                              className="text-xs border border-gray-200 rounded-lg px-2 py-1 focus:outline-none focus:ring-2 focus:ring-blue-400 disabled:bg-gray-50">
                              <option value="NORMAL">Normal</option>
                              <option value="URGENTE">Urgente</option>
                              <option value="CRITICO">Crítico</option>
                            </select>
                            <button onClick={() => cancelWaitlistEntry(w.id)} className={btnDanger}>
                              Cancelar
                            </button>
                          </div>
                        )}
                        {(w.status === "CANCELLED" || w.status === "ASSIGNED") && (
                          <span className={`text-xs font-semibold px-3 py-1 rounded-full ${PRIORITY_COLORS[w.priority] ?? "bg-gray-100 text-gray-600"}`}>
                            {PRIORITY_LABELS[w.priority] ?? w.priority}
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* ── Usuarios (Admin) ────────────────────────────────────────────── */}
        {tab === "usuarios" && (
          <div>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-bold text-gray-800">Usuarios registrados</h2>
              <Link href="/register/staff" className={btnPrimary}>+ Nuevo personal</Link>
            </div>
            {users.length === 0 ? (
              <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-12 text-center text-gray-400 text-sm">
                No hay usuarios
              </div>
            ) : (
              <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
                <table className="w-full text-sm">
                  <thead className="bg-gray-50 border-b border-gray-100">
                    <tr>
                      {["Nombre", "Correo", "Rol", "Estado"].map(h => (
                        <th key={h} className="text-left px-6 py-3 text-xs font-bold text-gray-400 uppercase tracking-wider">{h}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-50">
                    {users.map(u => (
                      <tr key={u.id} className="hover:bg-gray-50 transition-colors">
                        <td className="px-6 py-4 font-medium text-gray-800">{u.firstName} {u.lastName}</td>
                        <td className="px-6 py-4 text-gray-500">{u.email}</td>
                        <td className="px-6 py-4">
                          <span className="bg-blue-50 text-blue-700 text-xs font-semibold px-2 py-1 rounded-full">
                            {ROLE_LABELS[u.role] ?? u.role}
                          </span>
                        </td>
                        <td className="px-6 py-4">
                          <span className={`inline-flex items-center gap-1 text-xs font-semibold ${u.isActive ? "text-green-600" : "text-red-500"}`}>
                            <span className={`w-1.5 h-1.5 rounded-full ${u.isActive ? "bg-green-500" : "bg-red-400"}`} />
                            {u.isActive ? "Activo" : "Inactivo"}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
