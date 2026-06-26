"use client";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { SimpleNav } from "@/app/components/SimpleNav";
import Alert from "@/app/components/Alert";
import { DOC_TYPES, DOC_LABELS, STAFF_ROLES, ROLE_LABELS, ROLE_COLORS } from "@/features/users/users.constants";
import { inputClass } from "@/lib/styles";
import { getSession } from "@/lib/session";
import { BFF_URL } from "@/lib/bff";

const MEDICAL_SPECIALTIES = [
  "GENERAL", "CARDIOLOGY", "NEUROLOGY", "PEDIATRICS", "ORTHOPEDICS",
  "DERMATOLOGY", "GYNECOLOGY", "OPHTHALMOLOGY", "PSYCHIATRY",
  "TRAUMATOLOGY", "INTERNAL_MEDICINE", "EMERGENCY",
];
const SPECIALTY_LABELS: Record<string, string> = {
  GENERAL: "Medicina General", CARDIOLOGY: "Cardiología", NEUROLOGY: "Neurología",
  PEDIATRICS: "Pediatría", ORTHOPEDICS: "Ortopedia", DERMATOLOGY: "Dermatología",
  GYNECOLOGY: "Ginecología", OPHTHALMOLOGY: "Oftalmología", PSYCHIATRY: "Psiquiatría",
  TRAUMATOLOGY: "Traumatología", INTERNAL_MEDICINE: "Medicina Interna", EMERGENCY: "Urgencias",
};

export default function StaffRegisterPage() {
  const router = useRouter();
  const [authorized, setAuthorized] = useState(false);
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState("");
  const [error, setError] = useState("");
  const [form, setForm] = useState({
    firstName: "", lastName: "", email: "", password: "",
    phone: "", documentType: "DNI", documentNumber: "",
    role: "DOCTOR", specialty: "GENERAL",
  });

  useEffect(() => {
    const { token, role } = getSession();
    if (!token || role !== "ADMIN") {
      router.push("/dashboard");
      return;
    }
    setAuthorized(true);
  }, [router]);

  function set(field: string, value: string) {
    setForm((prev) => ({ ...prev, [field]: value }));
  }

  function resetForm() {
    setForm({
      firstName: "", lastName: "", email: "", password: "",
      phone: "", documentType: "DNI", documentNumber: "",
      role: "DOCTOR", specialty: "GENERAL",
    });
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError("");
    setSuccess("");

    try {
      const body = {
        ...form,
        specialty: form.role === "DOCTOR" ? form.specialty : undefined,
      };

      const res = await fetch(`${BFF_URL}/api/users`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });

      const data = await res.json();

      if (!res.ok) {
        setError(data.details?.error ?? data.message ?? "Error al registrar");
        return;
      }

      setSuccess(`${ROLE_LABELS[form.role]} ${data.firstName} ${data.lastName} registrado correctamente.`);
      resetForm();
    } catch {
      setError("Error de conexión. Intente nuevamente.");
    } finally {
      setLoading(false);
    }
  }

  if (!authorized) return null;

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <SimpleNav
        logoHref="/dashboard"
        logoSubtitle="Panel de administración"
        backHref="/dashboard"
        backLabel="← Volver al dashboard"
      />

      <div className="flex-1 flex items-center justify-center px-4 py-12">
        <div className="w-full max-w-lg">
          <div className="bg-white rounded-2xl shadow-xl border border-gray-100 p-8">
            <div className="inline-flex items-center gap-2 bg-orange-50 text-orange-700 text-xs font-bold px-3 py-1.5 rounded-full mb-5 tracking-wide">
              <span className="w-1.5 h-1.5 bg-orange-500 rounded-full" />
              SOLO ADMINISTRADORES
            </div>
            <h1 className="text-2xl font-bold text-gray-900 mb-1">Registrar personal</h1>
            <p className="text-sm text-gray-500 mb-7">
              Cree cuentas para médicos, enfermeros y personal del hospital
            </p>

            {success && <Alert variant="success" message={success} />}
            {error && <Alert variant="error" message={error} />}

            <form onSubmit={handleSubmit} className="flex flex-col gap-4">
              <div>
                <p className="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2">
                  Rol del personal
                </p>
                <div className="grid grid-cols-2 gap-2">
                  {STAFF_ROLES.map((r) => (
                    <button key={r} type="button" onClick={() => set("role", r)}
                      className={`py-2.5 px-4 rounded-xl text-sm font-semibold border-2 transition-all ${
                        form.role === r ? `${ROLE_COLORS[r]} border-current` : "border-gray-200 text-gray-400 hover:border-gray-300"
                      }`}>
                      {ROLE_LABELS[r]}
                    </button>
                  ))}
                </div>
              </div>

              {/* Especialidad — solo visible para DOCTOR */}
              {form.role === "DOCTOR" && (
                <div>
                  <label className="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2 block">
                    Especialidad médica
                  </label>
                  <select value={form.specialty} onChange={(e) => set("specialty", e.target.value)}
                    className={inputClass} required>
                    {MEDICAL_SPECIALTIES.map((s) => (
                      <option key={s} value={s}>{SPECIALTY_LABELS[s]}</option>
                    ))}
                  </select>
                </div>
              )}

              <div className="grid grid-cols-2 gap-3">
                <input placeholder="Nombre" value={form.firstName}
                  onChange={(e) => set("firstName", e.target.value)} className={inputClass} required />
                <input placeholder="Apellido" value={form.lastName}
                  onChange={(e) => set("lastName", e.target.value)} className={inputClass} required />
              </div>

              <input type="email" placeholder="Correo electrónico" value={form.email}
                onChange={(e) => set("email", e.target.value)} className={inputClass}
                required autoComplete="off" />

              <input type="password" placeholder="Contraseña temporal (mín. 8 caracteres)" value={form.password}
                onChange={(e) => set("password", e.target.value)} className={inputClass}
                required minLength={8} autoComplete="new-password" />

              <input type="tel" placeholder="Teléfono" value={form.phone}
                onChange={(e) => set("phone", e.target.value)} className={inputClass} required />

              <div className="grid grid-cols-2 gap-3">
                <select value={form.documentType} onChange={(e) => set("documentType", e.target.value)}
                  className={inputClass}>
                  {DOC_TYPES.map((t) => <option key={t} value={t}>{DOC_LABELS[t]}</option>)}
                </select>
                <input placeholder="Número de documento" value={form.documentNumber}
                  onChange={(e) => set("documentNumber", e.target.value)} className={inputClass} required />
              </div>

              <button type="submit" disabled={loading}
                className="w-full bg-gray-900 text-white py-3.5 rounded-xl text-sm font-semibold hover:bg-gray-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors mt-1">
                {loading ? "Registrando..." : `Registrar ${ROLE_LABELS[form.role]}`}
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
