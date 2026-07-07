import Link from "next/link";
import { HospitalLogo } from "@/app/components/HospitalLogo";

interface DashboardNavProps {
  email: string;
  role: string;
  onLogout: () => void;
}

export function DashboardNav({ email, role, onLogout }: DashboardNavProps) {
  return (
    <nav className="bg-white border-b border-gray-100 px-8 py-4 flex items-center justify-between shadow-sm">
      <HospitalLogo subtitle="Portal del Paciente" />
      <div className="flex items-center gap-4">
        <span className="text-sm text-gray-500">{email}</span>
        {role === "ADMIN" && (
          <Link
            href="/register/staff"
            className="bg-gray-900 text-white px-4 py-2 rounded-lg text-sm font-semibold hover:bg-gray-700 transition-colors"
          >
            + Registrar personal
          </Link>
        )}
        <button
          onClick={onLogout}
          className="text-sm text-red-500 hover:text-red-700 font-medium transition-colors"
        >
          Cerrar sesión
        </button>
      </div>
    </nav>
  );
}
