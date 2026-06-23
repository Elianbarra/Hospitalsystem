"use client";
import { useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";
import { Alert } from "./Alert";
import { BFF_URL } from "@/lib/bff";

type UserType = "PATIENT" | "STAFF";

export function LoginForm() {
  const [userType, setUserType]         = useState<UserType | null>(null);
  const [email, setEmail]               = useState("");
  const [password, setPassword]         = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [keepSession, setKeepSession]   = useState(false);
  const [loading, setLoading]           = useState(false);
  const [error, setError]               = useState("");
  const router = useRouter();
  const params = useSearchParams();
  const justRegistered = params.get("registered") === "1";

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError("");
    try {
      const res = await fetch(`${BFF_URL}/api/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });
      if (!res.ok) {
        setError("Credenciales inválidas. Por favor, ingresa correo y contraseña válidos.");
        return;
      }
      const data = await res.json();
      const role: string = data.role;
      const isStaff = role === "DOCTOR" || role === "ADMIN";
      if (userType === "PATIENT" && isStaff) {
        setError("Esta cuenta pertenece a un funcionario. Selecciona «Funcionario» para ingresar.");
        return;
      }
      if (userType === "STAFF" && !isStaff) {
        setError("Esta cuenta pertenece a un paciente. Selecciona «Paciente» para ingresar.");
        return;
      }
      const storage = keepSession ? localStorage : sessionStorage;
      storage.setItem("token", data.token);
      storage.setItem("userId", data.userId);
      storage.setItem("role", data.role);
      storage.setItem("email", data.email);
      router.push("/dashboard");
    } catch {
      setError("Error de conexión. Intente nuevamente.");
    } finally {
      setLoading(false);
    }
  }

  const canSubmit = email.trim() !== "" && password.trim() !== "" && !loading;

  // ── Paso 1: elegir tipo ────────────────────────────────────────────────────
  if (!userType) {
    return (
      <div className="flex flex-col gap-4">
        {justRegistered && (
          <Alert variant="success" message="Cuenta creada. Seleccione su tipo de acceso." />
        )}
        <p className="text-center text-sm text-gray-500 mb-1">¿Cómo desea ingresar?</p>

        <button type="button" onClick={() => setUserType("PATIENT")}
          className="flex items-start gap-4 border-2 border-gray-200 rounded-2xl px-5 py-4 text-left hover:border-blue-500 hover:bg-blue-50 transition-all group">
          <span className="text-3xl mt-0.5">🏥</span>
          <div>
            <p className="text-sm font-bold text-gray-800 group-hover:text-blue-700">Soy paciente</p>
            <p className="text-xs text-gray-400 mt-0.5">Ver mis citas y mi posición en lista de espera</p>
          </div>
        </button>

        <button type="button" onClick={() => setUserType("STAFF")}
          className="flex items-start gap-4 border-2 border-gray-200 rounded-2xl px-5 py-4 text-left hover:border-blue-500 hover:bg-blue-50 transition-all group">
          <span className="text-3xl mt-0.5">👨‍⚕️</span>
          <div>
            <p className="text-sm font-bold text-gray-800 group-hover:text-blue-700">Soy funcionario</p>
            <p className="text-xs text-gray-400 mt-0.5">Médico o administrativo del centro de salud</p>
          </div>
        </button>

        <div className="text-center mt-1 text-xs text-gray-400">
          ¿Paciente sin cuenta?{" "}
          <Link href="/register" className="text-blue-700 font-semibold hover:underline">
            Solicitar registro
          </Link>
        </div>
      </div>
    );
  }

  // ── Paso 2: formulario de login ────────────────────────────────────────────
  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <button type="button" onClick={() => { setUserType(null); setError(""); }}
          className="text-xs text-gray-400 hover:text-gray-600 transition-colors">
          ← Volver
        </button>
        <span className={`inline-flex items-center gap-1.5 text-xs font-semibold px-3 py-1.5 rounded-full ${
          userType === "PATIENT" ? "bg-blue-50 text-blue-700" : "bg-green-50 text-green-700"
        }`}>
          {userType === "PATIENT" ? "🏥 Paciente" : "👨‍⚕️ Funcionario"}
        </span>
      </div>

      {error && <Alert variant="error" message={error} />}

      <input
        type="email"
        placeholder={userType === "PATIENT" ? "Correo electrónico" : "Correo institucional"}
        value={email}
        onChange={e => setEmail(e.target.value)}
        className="w-full border border-gray-200 rounded-xl px-4 py-3.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 placeholder:text-gray-400"
        required autoComplete="email" autoFocus
      />

      <div className="relative">
        <input
          type={showPassword ? "text" : "password"}
          placeholder="Contraseña"
          value={password}
          onChange={e => setPassword(e.target.value)}
          className="w-full border border-gray-200 rounded-xl px-4 py-3.5 pr-12 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 placeholder:text-gray-400"
          required autoComplete="current-password"
        />
        <button type="button" onClick={() => setShowPassword(v => !v)}
          className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
          aria-label={showPassword ? "Ocultar contraseña" : "Mostrar contraseña"}>
          {showPassword ? (
            <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
            </svg>
          ) : (
            <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
            </svg>
          )}
        </button>
      </div>

      <div className="flex items-center justify-between">
        <label className="flex items-center gap-2 text-sm text-gray-600 cursor-pointer select-none">
          <input type="checkbox" checked={keepSession} onChange={e => setKeepSession(e.target.checked)}
            className="w-4 h-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500" />
          Mantener la sesión
        </label>
        <a href="#" className="text-sm text-blue-700 hover:underline">¿Olvidó su contraseña?</a>
      </div>

      <button type="submit" disabled={!canSubmit}
        className="w-full bg-gray-800 text-white py-3.5 rounded-xl text-sm font-semibold hover:bg-gray-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors">
        {loading ? "Iniciando sesión..." : "Iniciar sesión"}
      </button>

      <div className="flex items-center justify-between text-xs text-gray-400 pt-1">
        {userType === "PATIENT" ? (
          <Link href="/register" className="text-blue-700 font-semibold hover:underline">
            ¿Sin cuenta? Solicitar registro
          </Link>
        ) : (
          <span className="text-gray-400">Las cuentas de funcionarios son creadas por el administrador.</span>
        )}
        <span className="flex items-center gap-1 shrink-0 ml-2">
          <svg className="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
          </svg>
          TLS 1.3
        </span>
      </div>
    </form>
  );
}
