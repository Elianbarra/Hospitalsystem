"use client";
import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { SimpleNav } from "@/app/components/SimpleNav";
import Alert from "@/app/components/Alert";
import { DOC_TYPES, DOC_LABELS } from "@/features/users/users.constants";
import { inputClass } from "@/lib/styles";
import { BFF_URL } from "@/lib/bff";

export default function PatientRegisterPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    phone: "",
    documentType: "DNI",
    documentNumber: "",
  });

  function set(field: string, value: string) {
    setForm((prev) => ({ ...prev, [field]: value }));
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError("");

    try {
      const res = await fetch(`${BFF_URL}/api/users`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ...form, role: "PATIENT" }),
      });

      const data = await res.json();

      if (!res.ok) {
        setError(data.details?.error ?? data.message ?? "Error al registrar");
        return;
      }

      router.push("/?registered=1");
    } catch {
      setError("Error de conexión. Intente nuevamente.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <SimpleNav backHref="/" backLabel="← Volver al inicio" />

      <div className="flex-1 flex items-center justify-center px-4 py-12">
        <div className="w-full max-w-lg">
          <div className="bg-white rounded-2xl shadow-xl border border-gray-100 p-8">
            <div className="inline-flex items-center gap-2 bg-blue-50 text-blue-700 text-xs font-bold px-3 py-1.5 rounded-full mb-5 tracking-wide">
              <span className="w-1.5 h-1.5 bg-blue-500 rounded-full" />
              PORTAL DEL PACIENTE
            </div>
            <h1 className="text-2xl font-bold text-gray-900 mb-1">Crear cuenta</h1>
            <p className="text-sm text-gray-500 mb-7">Acceda a sus citas, historial clínico y más</p>

            {error && <Alert variant="error" message={error} />}

            <form onSubmit={handleSubmit} className="flex flex-col gap-4">
              <div className="grid grid-cols-2 gap-3">
                <input placeholder="Nombre" value={form.firstName}
                  onChange={(e) => set("firstName", e.target.value)} className={inputClass} required />
                <input placeholder="Apellido" value={form.lastName}
                  onChange={(e) => set("lastName", e.target.value)} className={inputClass} required />
              </div>

              <input type="email" placeholder="Correo electrónico" value={form.email}
                onChange={(e) => set("email", e.target.value)} className={inputClass}
                required autoComplete="email" />

              <input type="password" placeholder="Contraseña (mín. 8 caracteres)" value={form.password}
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
                className="w-full bg-blue-700 text-white py-3.5 rounded-xl text-sm font-semibold hover:bg-blue-800 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors mt-1">
                {loading ? "Creando cuenta..." : "Crear cuenta"}
              </button>

              <p className="text-center text-sm text-gray-500">
                ¿Ya tiene cuenta?{" "}
                <Link href="/" className="text-blue-700 font-semibold hover:underline">Iniciar sesión</Link>
              </p>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
