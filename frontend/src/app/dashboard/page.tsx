"use client";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { HospitalLogo } from "@/app/components/HospitalLogo";
import Alert from "@/app/components/Alert";
import { ROLE_LABELS, DOC_LABELS } from "@/features/users/users.constants";
import { getSession, clearSession } from "@/lib/session";
import { BFF_URL } from "@/lib/bff";

// Mapeo de especialidades ms-appointment → ms-waitlist
const APPT_TO_WAITLIST_SPECIALTY: Record<string, string> = {
  CARDIOLOGY: "CARDIOLOGIA",
  NEUROLOGY: "NEUROLOGIA",
  PEDIATRICS: "PEDIATRIA",
  ORTHOPEDICS: "ORTOPEDIA",
  DERMATOLOGY: "DERMATOLOGIA",
  GYNECOLOGY: "GINECOLOGIA",
  OPHTHALMOLOGY: "OFTALMOLOGIA",
  PSYCHIATRY: "PSIQUIATRIA",
  TRAUMATOLOGY: "TRAUMATOLOGIA",
  INTERNAL_MEDICINE: "MEDICINA_INTERNA",
  EMERGENCY: "EMERGENCIAS",
  GENERAL: "GENERAL",
};

const SPECIALTY_LABELS: Record<string, string> = {
  CARDIOLOGY: "Cardiología",
  NEUROLOGY: "Neurología",
  PEDIATRICS: "Pediatría",
  ORTHOPEDICS: "Ortopedia",
  DERMATOLOGY: "Dermatología",
  GYNECOLOGY: "Ginecología",
  OPHTHALMOLOGY: "Oftalmología",
  PSYCHIATRY: "Psiquiatría",
  TRAUMATOLOGY: "Traumatología",
  INTERNAL_MEDICINE: "Medicina Interna",
  EMERGENCY: "Urgencias",
  GENERAL: "Medicina General",
};

interface UserProfile {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  documentType: string;
  documentNumber: string;
  role: string;
  specialty?: string;
  isActive: boolean;
  createdAt: string;
}

interface Doctor {
  id: string;
  firstName: string;
  lastName: string;
  specialty: string;
}

interface Appointment {
  id: string;
  patientId: string;
  doctorId: string;
  specialty: string;
  appointmentType: string;
  scheduledAt: string;
  status: string;
  cancelledBy?: string;
  notes?: string;
}

interface WaitlistEntry {
  id: string;
  patientId: string;
  specialty: string;
  appointmentType: string;
  priority: string;
  vitalRisk: boolean;
  status: string;
  notes?: string;
  createdAt: string;
  requeuedAt: string;
}

type Tab = "profile" | "appointments" | "waitlist";

function SlotPicker({
  slots,
  selectedSlot,
  onSelect,
}: {
  slots: string[];
  selectedSlot: string | null;
  onSelect: (iso: string) => void;
}) {
  const displayed = slots.slice(0, 20);
  const byDate: Record<string, string[]> = {};
  for (const iso of displayed) {
    const d = new Date(iso);
    const key = d.toLocaleDateString("es-ES", {
      weekday: "long",
      year: "numeric",
      month: "long",
      day: "numeric",
    });
    if (!byDate[key]) byDate[key] = [];
    byDate[key].push(iso);
  }
  return (
    <div className="border border-gray-200 rounded-xl overflow-hidden">
      <div className="max-h-64 overflow-y-auto">
        {Object.entries(byDate).map(([dateLabel, dateSlots]) => (
          <div key={dateLabel}>
            <div className="bg-gray-50 px-4 py-2 text-xs font-bold text-gray-500 uppercase tracking-wider sticky top-0 capitalize">
              {dateLabel}
            </div>
            <div className="flex flex-wrap gap-2 px-4 py-3">
              {dateSlots.map((iso) => {
                const label = new Date(iso).toLocaleTimeString("es-ES", {
                  hour: "2-digit",
                  minute: "2-digit",
                });
                return (
                  <button
                    key={iso}
                    type="button"
                    onClick={() => onSelect(iso)}
                    className={`px-3 py-1.5 rounded-lg text-sm font-semibold border-2 transition-all ${
                      selectedSlot === iso
                        ? "bg-blue-700 text-white border-blue-700"
                        : "border-gray-200 text-gray-600 hover:border-blue-400 hover:text-blue-700"
                    }`}
                  >
                    {label}
                  </button>
                );
              })}
            </div>
          </div>
        ))}
      </div>
      {slots.length > 20 && (
        <div className="bg-gray-50 px-4 py-2 text-xs text-gray-400 text-center border-t border-gray-100">
          Mostrando los primeros 20 horarios disponibles
        </div>
      )}
    </div>
  );
}

export default function Dashboard() {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [sessionEmail, setSessionEmail] = useState("");
  const [sessionRole, setSessionRole] = useState("");
  const [sessionUserId, setSessionUserId] = useState("");
  const [sessionToken, setSessionToken] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [activeTab, setActiveTab] = useState<Tab>("profile");
  const router = useRouter();

  // ── Citas ─────────────────────────────────────────────────────────────────
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [apptLoading, setApptLoading] = useState(false);
  const [apptError, setApptError] = useState("");
  const [showApptForm, setShowApptForm] = useState(false);
  const [apptSpecialty, setApptSpecialty] = useState("");
  const [doctors, setDoctors] = useState<Doctor[]>([]);
  const [apptDoctor, setApptDoctor] = useState("");
  const [availableSlots, setAvailableSlots] = useState<string[]>([]);
  const [selectedSlot, setSelectedSlot] = useState<string | null>(null);
  const [slotsLoading, setSlotsLoading] = useState(false);
  const [waitlistPosition, setWaitlistPosition] = useState<number | null>(null);
  const [apptType, setApptType] = useState<"CONSULTA" | "CIRUGIA">("CONSULTA");
  const [apptNotes, setApptNotes] = useState("");
  const [apptSubmitting, setApptSubmitting] = useState(false);
  const [apptSuccess, setApptSuccess] = useState("");

  // ── Lista de espera ────────────────────────────────────────────────────────
  const [waitlist, setWaitlist] = useState<WaitlistEntry[]>([]);
  const [wlLoading, setWlLoading] = useState(false);
  const [wlError, setWlError] = useState("");
  const [showWlForm, setShowWlForm] = useState(false);
  const [wlSpecialty, setWlSpecialty] = useState("");
  const [wlType, setWlType] = useState<"CONSULTA" | "CIRUGIA">("CONSULTA");
  const [wlVitalRisk, setWlVitalRisk] = useState(false);
  const [wlNotes, setWlNotes] = useState("");
  const [wlSubmitting, setWlSubmitting] = useState(false);
  const [wlSuccess, setWlSuccess] = useState("");

  useEffect(() => {
    const { token, userId, email, role } = getSession();
    if (!token || !userId) {
      router.push("/");
      return;
    }
    setSessionEmail(email);
    setSessionRole(role);
    setSessionUserId(userId);
    setSessionToken(token);

    fetch(`${BFF_URL}/api/users/${userId}`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => {
        if (!res.ok) throw new Error("No se pudo cargar el perfil");
        return res.json();
      })
      .then(setUser)
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoading(false));
  }, [router]);

  // ── Cargar citas del usuario ───────────────────────────────────────────────
  function loadAppointments() {
    setApptLoading(true);
    setApptError("");
    const endpoint =
      sessionRole === "DOCTOR"
        ? `${BFF_URL}/api/appointments/doctor/${sessionUserId}`
        : `${BFF_URL}/api/appointments/patient/${sessionUserId}`;
    fetch(endpoint, { headers: { Authorization: `Bearer ${sessionToken}` } })
      .then((r) => r.json())
      .then(setAppointments)
      .catch(() => setApptError("No se pudieron cargar las citas"))
      .finally(() => setApptLoading(false));
  }

  // ── Cargar médicos por especialidad + posición en lista de espera ──────────
  async function onSpecialtyChange(specialty: string) {
    setApptSpecialty(specialty);
    setApptDoctor("");
    setAvailableSlots([]);
    setSelectedSlot(null);
    setWaitlistPosition(null);
    setDoctors([]);
    if (!specialty) return;

    try {
      const [docRes, wlRes] = await Promise.all([
        fetch(`${BFF_URL}/api/users/specialty/${specialty}`, {
          headers: { Authorization: `Bearer ${sessionToken}` },
        }),
        fetch(
          `${BFF_URL}/api/waitlist/specialty/${
            APPT_TO_WAITLIST_SPECIALTY[specialty] ?? specialty
          }`,
          {
            headers: { Authorization: `Bearer ${sessionToken}` },
          }
        ),
      ]);
      if (docRes.ok) setDoctors(await docRes.json());
      if (wlRes.ok) {
        const wlList: WaitlistEntry[] = await wlRes.json();
        const pos = wlList.findIndex(
          (e) => e.patientId === sessionUserId && e.status === "WAITING"
        );
        setWaitlistPosition(pos >= 0 ? pos + 1 : null);
      }
    } catch {
      /* silencioso */
    }
  }

  // ── Al elegir médico, calcular slots disponibles ──────────────────────────
  async function onDoctorChange(doctorId: string) {
    setApptDoctor(doctorId);
    setAvailableSlots([]);
    setSelectedSlot(null);
    if (!doctorId) return;
    setSlotsLoading(true);
    try {
      const res = await fetch(
        `${BFF_URL}/api/appointments/doctor/${doctorId}`,
        {
          headers: { Authorization: `Bearer ${sessionToken}` },
        }
      );
      if (!res.ok) return;
      const appts: Appointment[] = await res.json();
      setAvailableSlots(findAvailableSlots(appts));
    } catch {
      /* silencioso */
    } finally {
      setSlotsLoading(false);
    }
  }

  // ── Slots libres: Lun-Vie, 09:00-11:00 y 14:00-16:00, bloques 30 min, hasta 6 meses ──
  function findAvailableSlots(appts: Appointment[]): string[] {
    const MORNING = [
      [9, 0],
      [9, 30],
      [10, 0],
      [10, 30],
    ];
    const AFTERNOON = [
      [14, 0],
      [14, 30],
      [15, 0],
      [15, 30],
    ];
    const allBlocks = [...MORNING, ...AFTERNOON];

    const now = new Date();
    const sixMonthsLater = new Date(now);
    sixMonthsLater.setMonth(sixMonthsLater.getMonth() + 6);

    const occupiedSet = new Set(
      appts
        .filter((a) => a.status !== "CANCELLED")
        .map((a) => {
          const d = new Date(a.scheduledAt);
          // Normalise to minute precision to match generated slots
          d.setSeconds(0);
          d.setMilliseconds(0);
          return d.getTime();
        })
    );

    const slots: string[] = [];
    const cursor = new Date(now);
    cursor.setSeconds(0);
    cursor.setMilliseconds(0);

    while (cursor <= sixMonthsLater) {
      const day = cursor.getDay(); // 0=Dom, 6=Sáb
      if (day !== 0 && day !== 6) {
        for (const [h, m] of allBlocks) {
          const slot = new Date(cursor);
          slot.setHours(h, m, 0, 0);
          if (
            slot > now &&
            slot <= sixMonthsLater &&
            !occupiedSet.has(slot.getTime())
          ) {
            slots.push(slot.toISOString());
          }
        }
      }
      cursor.setDate(cursor.getDate() + 1);
    }
    return slots;
  }

  // ── Crear cita ─────────────────────────────────────────────────────────────
  async function handleCreateAppointment(e: React.FormEvent) {
    e.preventDefault();
    if (!apptDoctor || !selectedSlot || !apptSpecialty) return;
    setApptSubmitting(true);
    setApptError("");
    try {
      const body = {
        patientId: sessionUserId,
        doctorId: apptDoctor,
        specialty: apptSpecialty,
        appointmentType: apptType,
        scheduledAt: selectedSlot,
        notes: apptNotes || undefined,
      };
      const res = await fetch(`${BFF_URL}/api/appointments`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${sessionToken}`,
        },
        body: JSON.stringify(body),
      });
      if (!res.ok) {
        const d = await res.json();
        throw new Error(d.message ?? "Error al crear cita");
      }
      setApptSuccess("Cita creada exitosamente");
      setShowApptForm(false);
      setApptSpecialty("");
      setApptDoctor("");
      setAvailableSlots([]);
      setSelectedSlot(null);
      setApptNotes("");
      setApptType("CONSULTA");
      loadAppointments();
    } catch (err: unknown) {
      setApptError(err instanceof Error ? err.message : "Error inesperado");
    } finally {
      setApptSubmitting(false);
    }
  }

  // ── Cancelar cita (médico o paciente) ─────────────────────────────────────
  async function cancelAppointment(appt: Appointment) {
    const endpoint =
      sessionRole === "DOCTOR"
        ? `${BFF_URL}/api/reassignment/cancel-doctor/${appt.id}`
        : `${BFF_URL}/api/reassignment/cancel-patient/${appt.id}`;

    try {
      const res = await fetch(endpoint, {
        method: "PUT",
        headers: { Authorization: `Bearer ${sessionToken}` },
      });
      if (!res.ok) throw new Error("No se pudo cancelar la cita");
      loadAppointments();
    } catch (err: unknown) {
      setApptError(err instanceof Error ? err.message : "Error al cancelar");
    }
  }

  // ── Cargar lista de espera ─────────────────────────────────────────────────
  function loadWaitlist() {
    setWlLoading(true);
    setWlError("");
    const endpoint =
      sessionRole === "DOCTOR" || sessionRole === "ADMIN"
        ? `${BFF_URL}/api/waitlist`
        : `${BFF_URL}/api/waitlist/patient/${sessionUserId}`;
    fetch(endpoint, { headers: { Authorization: `Bearer ${sessionToken}` } })
      .then((r) => r.json())
      .then(setWaitlist)
      .catch(() => setWlError("No se pudo cargar la lista de espera"))
      .finally(() => setWlLoading(false));
  }

  // ── Inscribir en lista de espera ──────────────────────────────────────────
  async function handleJoinWaitlist(e: React.FormEvent) {
    e.preventDefault();
    if (!wlSpecialty) return;
    setWlSubmitting(true);
    setWlError("");
    try {
      const body = {
        patientId: sessionUserId,
        specialty: APPT_TO_WAITLIST_SPECIALTY[wlSpecialty] ?? wlSpecialty,
        appointmentType: wlType,
        vitalRisk: wlVitalRisk,
        notes: wlNotes || undefined,
      };
      const res = await fetch(`${BFF_URL}/api/waitlist`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${sessionToken}`,
        },
        body: JSON.stringify(body),
      });
      if (!res.ok) {
        const d = await res.json();
        throw new Error(d.message ?? "Error al inscribirse");
      }
      setWlSuccess("Inscrito en lista de espera");
      setShowWlForm(false);
      setWlSpecialty("");
      setWlType("CONSULTA");
      setWlVitalRisk(false);
      setWlNotes("");
      loadWaitlist();
    } catch (err: unknown) {
      setWlError(err instanceof Error ? err.message : "Error inesperado");
    } finally {
      setWlSubmitting(false);
    }
  }

  // ── Actualizar vitalRisk (médico/admin) ───────────────────────────────────
  async function updateVitalRisk(entryId: string, vitalRisk: boolean) {
    try {
      await fetch(`${BFF_URL}/api/waitlist/${entryId}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${sessionToken}`,
        },
        body: JSON.stringify({ vitalRisk }),
      });
      loadWaitlist();
    } catch {
      setWlError("No se pudo actualizar el riesgo vital");
    }
  }

  // ── Cancelar entrada en waitlist ──────────────────────────────────────────
  async function cancelWaitlistEntry(entryId: string) {
    try {
      await fetch(`${BFF_URL}/api/waitlist/${entryId}/cancel`, {
        method: "PUT",
        headers: { Authorization: `Bearer ${sessionToken}` },
      });
      loadWaitlist();
    } catch {
      setWlError("No se pudo cancelar la entrada");
    }
  }

  useEffect(() => {
    if (!sessionUserId) return;
    if (activeTab === "appointments") loadAppointments();
    if (activeTab === "waitlist") loadWaitlist();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeTab, sessionUserId]);

  const initials = user
    ? `${user.firstName[0]}${user.lastName[0]}`.toUpperCase()
    : "??";

  const isStaff =
    sessionRole === "DOCTOR" ||
    sessionRole === "ADMIN" ||
    sessionRole === "NURSE" ||
    sessionRole === "RECEPTIONIST";

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <nav className="bg-white border-b border-gray-100 px-8 py-4 flex items-center justify-between shadow-sm">
        <HospitalLogo subtitle="Portal del Paciente" />
        <div className="flex items-center gap-4">
          <span className="text-sm text-gray-500">{sessionEmail}</span>
          {sessionRole === "ADMIN" && (
            <Link
              href="/register/staff"
              className="bg-gray-900 text-white px-4 py-2 rounded-lg text-sm font-semibold hover:bg-gray-700 transition-colors"
            >
              + Registrar personal
            </Link>
          )}
          <button
            onClick={() => {
              clearSession();
              router.push("/");
            }}
            className="text-sm text-red-500 hover:text-red-700 font-medium transition-colors"
          >
            Cerrar sesión
          </button>
        </div>
      </nav>

      <div className="max-w-4xl mx-auto px-6 py-8 w-full">
        {/* Tabs */}
        <div className="flex gap-1 bg-white border border-gray-100 rounded-xl p-1 mb-8 shadow-sm w-fit">
          {(["profile", "appointments", "waitlist"] as Tab[]).map((tab) => {
            const labels: Record<Tab, string> = {
              profile: "Mi perfil",
              appointments: "Mis citas",
              waitlist: "Lista de espera",
            };
            return (
              <button
                key={tab}
                onClick={() => setActiveTab(tab)}
                className={`px-5 py-2 rounded-lg text-sm font-semibold transition-all ${
                  activeTab === tab
                    ? "bg-blue-700 text-white shadow"
                    : "text-gray-500 hover:text-gray-700"
                }`}
              >
                {labels[tab]}
              </button>
            );
          })}
        </div>

        {error && <Alert variant="error" message={error} className="mb-4" />}
        {loading && activeTab === "profile" && (
          <div className="bg-white rounded-2xl p-12 text-center text-gray-400 text-sm">
            Cargando perfil...
          </div>
        )}

        {/* ── PERFIL ─────────────────────────────────────────────────────── */}
        {activeTab === "profile" && user && (
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
            <div className="bg-gradient-to-r from-blue-700 to-blue-600 px-8 py-8 flex items-center gap-5">
              <div className="w-16 h-16 bg-white/20 rounded-full flex items-center justify-center text-white text-2xl font-black">
                {initials}
              </div>
              <div>
                <h2 className="text-xl font-bold text-white">
                  {user.firstName} {user.lastName}
                </h2>
                <span className="inline-block bg-white/20 text-white text-xs font-semibold px-3 py-1 rounded-full mt-1.5">
                  {ROLE_LABELS[user.role] ?? user.role}
                </span>
                {user.specialty && (
                  <span className="ml-2 inline-block bg-white/10 text-white text-xs font-semibold px-3 py-1 rounded-full">
                    {SPECIALTY_LABELS[user.specialty] ?? user.specialty}
                  </span>
                )}
              </div>
              <div className="ml-auto">
                <span
                  className={`inline-flex items-center gap-1.5 text-xs font-semibold px-3 py-1.5 rounded-full ${
                    user.isActive
                      ? "bg-green-400/20 text-green-100"
                      : "bg-red-400/20 text-red-100"
                  }`}
                >
                  <span
                    className={`w-1.5 h-1.5 rounded-full ${
                      user.isActive ? "bg-green-300" : "bg-red-300"
                    }`}
                  />
                  {user.isActive ? "Activo" : "Inactivo"}
                </span>
              </div>
            </div>
            <dl className="grid grid-cols-2 gap-px bg-gray-100">
              {[
                ["Correo electrónico", user.email],
                ["Teléfono", user.phone],
                [
                  "Tipo de documento",
                  DOC_LABELS[user.documentType as keyof typeof DOC_LABELS] ??
                    user.documentType,
                ],
                ["Número de documento", user.documentNumber],
                [
                  "Miembro desde",
                  new Date(user.createdAt).toLocaleDateString("es-ES", {
                    year: "numeric",
                    month: "long",
                    day: "numeric",
                  }),
                ],
                ["ID de usuario", user.id],
              ].map(([label, value]) => (
                <div key={label} className="bg-white px-8 py-5">
                  <dt className="text-xs font-bold text-gray-400 uppercase tracking-wider mb-1">
                    {label}
                  </dt>
                  <dd className="text-sm font-medium text-gray-800 truncate">
                    {value}
                  </dd>
                </div>
              ))}
            </dl>
          </div>
        )}

        {/* ── CITAS ──────────────────────────────────────────────────────── */}
        {activeTab === "appointments" && (
          <div>
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-lg font-bold text-gray-900">
                {sessionRole === "DOCTOR"
                  ? "Citas asignadas"
                  : "Mis citas médicas"}
              </h2>
              {!isStaff && (
                <button
                  onClick={() => {
                    setShowApptForm(!showApptForm);
                    setApptSuccess("");
                    setApptError("");
                  }}
                  className="bg-blue-700 text-white px-4 py-2 rounded-lg text-sm font-semibold hover:bg-blue-800 transition-colors"
                >
                  {showApptForm ? "Cancelar" : "+ Nueva cita"}
                </button>
              )}
            </div>

            {apptSuccess && (
              <Alert variant="success" message={apptSuccess} className="mb-4" />
            )}
            {apptError && (
              <Alert variant="error" message={apptError} className="mb-4" />
            )}

            {/* Formulario nueva cita */}
            {showApptForm && !isStaff && (
              <div className="bg-white border border-gray-100 rounded-2xl p-6 mb-6 shadow-sm">
                <h3 className="text-sm font-bold text-gray-700 mb-4">
                  Solicitar nueva cita
                </h3>
                <form
                  onSubmit={handleCreateAppointment}
                  className="flex flex-col gap-4"
                >
                  {/* Paso 1: Especialidad */}
                  <div>
                    <label className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-1 block">
                      1. Selecciona la especialidad
                    </label>
                    <select
                      value={apptSpecialty}
                      onChange={(e) => onSpecialtyChange(e.target.value)}
                      className="w-full border border-gray-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    >
                      <option value="">— Especialidad —</option>
                      {Object.entries(SPECIALTY_LABELS).map(([k, v]) => (
                        <option key={k} value={k}>
                          {v}
                        </option>
                      ))}
                    </select>
                    {waitlistPosition !== null && (
                      <p className="text-xs text-amber-600 font-semibold mt-2">
                        📋 Estás en posición #{waitlistPosition} de la lista de
                        espera para esta especialidad
                      </p>
                    )}
                    {waitlistPosition === null && apptSpecialty && (
                      <p className="text-xs text-gray-400 mt-2">
                        No estás en lista de espera para esta especialidad
                      </p>
                    )}
                  </div>

                  {/* Paso 2: Médico */}
                  {apptSpecialty && (
                    <div>
                      <label className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-1 block">
                        2. Selecciona el médico
                      </label>
                      {doctors.length === 0 ? (
                        <p className="text-sm text-gray-400">
                          No hay médicos disponibles para esta especialidad
                        </p>
                      ) : (
                        <select
                          value={apptDoctor}
                          onChange={(e) => onDoctorChange(e.target.value)}
                          className="w-full border border-gray-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                        >
                          <option value="">— Selecciona médico —</option>
                          {doctors.map((d) => (
                            <option key={d.id} value={d.id}>
                              Dr. {d.firstName} {d.lastName}
                            </option>
                          ))}
                        </select>
                      )}
                    </div>
                  )}

                  {/* Paso 3: Selector de horario */}
                  {apptDoctor && (
                    <div>
                      <label className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2 block">
                        3. Selecciona un horario disponible
                      </label>
                      {slotsLoading && (
                        <p className="text-sm text-gray-400">
                          Buscando horarios disponibles...
                        </p>
                      )}
                      {!slotsLoading && availableSlots.length === 0 && (
                        <p className="text-sm text-gray-400">
                          No hay horarios disponibles en los próximos 6 meses
                        </p>
                      )}
                      {!slotsLoading && availableSlots.length > 0 && (
                        <SlotPicker
                          slots={availableSlots}
                          selectedSlot={selectedSlot}
                          onSelect={setSelectedSlot}
                        />
                      )}
                      {selectedSlot && (
                        <p className="text-xs text-blue-700 font-semibold mt-2">
                          ✓ Seleccionado:{" "}
                          {new Date(selectedSlot).toLocaleString("es-ES", {
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

                  {/* Tipo de atención */}
                  {apptDoctor && (
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
                              onClick={() => setApptType(t)}
                              className={`flex-1 py-2.5 px-4 rounded-xl text-sm font-semibold border-2 transition-all ${
                                apptType === t
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
                        value={apptNotes}
                        onChange={(e) => setApptNotes(e.target.value)}
                        rows={2}
                        className="border border-gray-200 rounded-xl px-4 py-3 text-sm resize-none focus:outline-none focus:ring-2 focus:ring-blue-500"
                      />

                      <button
                        type="submit"
                        disabled={apptSubmitting || !selectedSlot}
                        className="w-full bg-blue-700 text-white py-3.5 rounded-xl text-sm font-semibold hover:bg-blue-800 disabled:bg-gray-200 disabled:cursor-not-allowed transition-colors"
                      >
                        {apptSubmitting ? "Solicitando..." : "Confirmar cita"}
                      </button>
                    </>
                  )}
                </form>
              </div>
            )}

            {/* Lista de citas */}
            {apptLoading && (
              <p className="text-sm text-gray-400">Cargando citas...</p>
            )}
            {!apptLoading && appointments.length === 0 && (
              <div className="bg-white rounded-2xl p-12 text-center text-gray-400 text-sm border border-gray-100">
                No hay citas registradas
              </div>
            )}
            <div className="flex flex-col gap-3">
              {appointments.map((a) => {
                const isCancelled = a.status === "CANCELLED";
                const statusColors: Record<string, string> = {
                  PENDING: "bg-yellow-50 text-yellow-700",
                  CONFIRMED: "bg-green-50 text-green-700",
                  CANCELLED: "bg-red-50 text-red-500",
                  COMPLETED: "bg-gray-50 text-gray-500",
                };
                return (
                  <div
                    key={a.id}
                    className="bg-white border border-gray-100 rounded-2xl p-5 shadow-sm flex items-start justify-between gap-4"
                  >
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-1">
                        <span
                          className={`text-xs font-bold px-2 py-0.5 rounded-full ${
                            statusColors[a.status] ??
                            "bg-gray-100 text-gray-600"
                          }`}
                        >
                          {a.status}
                        </span>
                        <span className="text-xs bg-purple-50 text-purple-700 font-semibold px-2 py-0.5 rounded-full">
                          {a.appointmentType === "CIRUGIA"
                            ? "🔬 Cirugía"
                            : "🩺 Consulta"}
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
                        onClick={() => cancelAppointment(a)}
                        className="text-xs text-red-500 hover:text-red-700 font-semibold shrink-0 transition-colors"
                      >
                        {sessionRole === "DOCTOR"
                          ? "Cancelar y reasignar"
                          : "Cancelar"}
                      </button>
                    )}
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* ── LISTA DE ESPERA ────────────────────────────────────────────── */}
        {activeTab === "waitlist" && (
          <div>
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-lg font-bold text-gray-900">
                {sessionRole === "DOCTOR" || sessionRole === "ADMIN"
                  ? "Lista de espera (todas las especialidades)"
                  : "Mi lista de espera"}
              </h2>
              {!isStaff && (
                <button
                  onClick={() => {
                    setShowWlForm(!showWlForm);
                    setWlSuccess("");
                    setWlError("");
                  }}
                  className="bg-green-700 text-white px-4 py-2 rounded-lg text-sm font-semibold hover:bg-green-800 transition-colors"
                >
                  {showWlForm ? "Cancelar" : "+ Unirse a lista de espera"}
                </button>
              )}
            </div>

            {wlSuccess && (
              <Alert variant="success" message={wlSuccess} className="mb-4" />
            )}
            {wlError && (
              <Alert variant="error" message={wlError} className="mb-4" />
            )}

            {/* Formulario inscripción en lista de espera */}
            {showWlForm && !isStaff && (
              <div className="bg-white border border-gray-100 rounded-2xl p-6 mb-6 shadow-sm">
                <h3 className="text-sm font-bold text-gray-700 mb-4">
                  Inscribirse en lista de espera
                </h3>
                <form
                  onSubmit={handleJoinWaitlist}
                  className="flex flex-col gap-4"
                >
                  <div>
                    <label className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-1 block">
                      Especialidad
                    </label>
                    <select
                      value={wlSpecialty}
                      onChange={(e) => setWlSpecialty(e.target.value)}
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
                          onClick={() => setWlType(t)}
                          className={`flex-1 py-2.5 px-4 rounded-xl text-sm font-semibold border-2 transition-all ${
                            wlType === t
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
                    value={wlNotes}
                    onChange={(e) => setWlNotes(e.target.value)}
                    rows={2}
                    className="border border-gray-200 rounded-xl px-4 py-3 text-sm resize-none focus:outline-none focus:ring-2 focus:ring-green-500"
                  />

                  <button
                    type="submit"
                    disabled={wlSubmitting || !wlSpecialty}
                    className="w-full bg-green-700 text-white py-3.5 rounded-xl text-sm font-semibold hover:bg-green-800 disabled:bg-gray-200 disabled:cursor-not-allowed transition-colors"
                  >
                    {wlSubmitting ? "Inscribiendo..." : "Confirmar inscripción"}
                  </button>
                </form>
              </div>
            )}

            {/* Lista de entradas */}
            {wlLoading && (
              <p className="text-sm text-gray-400">
                Cargando lista de espera...
              </p>
            )}
            {!wlLoading && waitlist.length === 0 && (
              <div className="bg-white rounded-2xl p-12 text-center text-gray-400 text-sm border border-gray-100">
                No hay entradas en lista de espera
              </div>
            )}
            <div className="flex flex-col gap-3">
              {waitlist.map((entry, idx) => {
                const priorityColors: Record<string, string> = {
                  CRITICO: "bg-red-100 text-red-700",
                  URGENTE: "bg-orange-100 text-orange-700",
                  NORMAL: "bg-gray-100 text-gray-600",
                };
                const statusColors: Record<string, string> = {
                  WAITING: "bg-blue-50 text-blue-700",
                  OFFERED: "bg-amber-50 text-amber-700",
                  NOTIFIED: "bg-yellow-50 text-yellow-700",
                  ASSIGNED: "bg-green-50 text-green-700",
                  CANCELLED: "bg-red-50 text-red-500",
                };
                return (
                  <div
                    key={entry.id}
                    className="bg-white border border-gray-100 rounded-2xl p-5 shadow-sm"
                  >
                    <div className="flex items-start justify-between gap-4">
                      <div className="flex-1">
                        <div className="flex items-center gap-2 flex-wrap mb-1">
                          {(sessionRole === "DOCTOR" ||
                            sessionRole === "ADMIN") && (
                            <span className="text-xs font-bold text-gray-300">
                              #{idx + 1}
                            </span>
                          )}
                          <span
                            className={`text-xs font-bold px-2 py-0.5 rounded-full ${
                              priorityColors[entry.priority] ??
                              "bg-gray-100 text-gray-600"
                            }`}
                          >
                            {entry.priority}
                          </span>
                          <span
                            className={`text-xs font-semibold px-2 py-0.5 rounded-full ${
                              statusColors[entry.status] ??
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
                            {SPECIALTY_LABELS[entry.specialty] ??
                              entry.specialty}
                          </span>
                        </div>
                        <p className="text-xs text-gray-400 mt-1">
                          Inscrito:{" "}
                          {new Date(entry.createdAt).toLocaleDateString(
                            "es-ES"
                          )}
                          {entry.requeuedAt &&
                            entry.requeuedAt !== entry.createdAt && (
                              <>
                                {" "}
                                · Reubicado:{" "}
                                {new Date(entry.requeuedAt).toLocaleString(
                                  "es-ES"
                                )}
                              </>
                            )}
                        </p>
                        {entry.notes && (
                          <p className="text-xs text-gray-500 mt-1">
                            {entry.notes}
                          </p>
                        )}

                        {/* vitalRisk toggle — solo médico o admin */}
                        {(sessionRole === "DOCTOR" ||
                          sessionRole === "ADMIN") &&
                          entry.status !== "CANCELLED" && (
                            <div className="flex items-center gap-3 mt-3">
                              <span className="text-xs font-semibold text-gray-500">
                                Riesgo vital:
                              </span>
                              <button
                                onClick={() =>
                                  updateVitalRisk(entry.id, !entry.vitalRisk)
                                }
                                className={`relative inline-flex h-5 w-9 items-center rounded-full transition-colors ${
                                  entry.vitalRisk ? "bg-red-500" : "bg-gray-200"
                                }`}
                              >
                                <span
                                  className={`inline-block h-4 w-4 transform rounded-full bg-white shadow transition-transform ${
                                    entry.vitalRisk
                                      ? "translate-x-4"
                                      : "translate-x-0.5"
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

                      {/* Cancelar entrada */}
                      {entry.status !== "CANCELLED" && (
                        <button
                          onClick={() => cancelWaitlistEntry(entry.id)}
                          className="text-xs text-red-500 hover:text-red-700 font-semibold shrink-0 transition-colors"
                        >
                          Cancelar
                        </button>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
