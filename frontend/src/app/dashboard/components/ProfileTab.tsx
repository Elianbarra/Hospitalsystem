import { DOC_LABELS, ROLE_LABELS } from "@/features/users/users.constants";
import { SPECIALTY_LABELS } from "@/features/appointments/appointments.constants";
import type { UserProfile } from "@/features/users/users.types";

interface ProfileTabProps {
  user: UserProfile;
}

export function ProfileTab({ user }: ProfileTabProps) {
  const initials = `${user.firstName[0]}${user.lastName[0]}`.toUpperCase();

  const fields: [string, string][] = [
    ["Correo electrónico", user.email],
    ["Teléfono", user.phone],
    [
      "Tipo de documento",
      DOC_LABELS[user.documentType as keyof typeof DOC_LABELS] ?? user.documentType,
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
  ];

  return (
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
        {fields.map(([label, value]) => (
          <div key={label} className="bg-white px-8 py-5">
            <dt className="text-xs font-bold text-gray-400 uppercase tracking-wider mb-1">
              {label}
            </dt>
            <dd className="text-sm font-medium text-gray-800 truncate">{value}</dd>
          </div>
        ))}
      </dl>
    </div>
  );
}
