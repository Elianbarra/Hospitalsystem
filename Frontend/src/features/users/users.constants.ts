export const DOC_TYPES = ["DNI", "PASSPORT", "FOREIGN_ID", "RUC"] as const;
export type DocumentType = (typeof DOC_TYPES)[number];

export const STAFF_ROLES = ["DOCTOR", "NURSE", "ADMIN", "RECEPTIONIST"] as const;
export type StaffRole = (typeof STAFF_ROLES)[number];

export const DOC_LABELS: Record<DocumentType, string> = {
  DNI: "DNI",
  PASSPORT: "Pasaporte",
  FOREIGN_ID: "Identificación Extranjera",
  RUC: "RUC",
};

export const ROLE_LABELS: Record<string, string> = {
  PATIENT: "Paciente",
  DOCTOR: "Médico",
  NURSE: "Enfermero/a",
  ADMIN: "Administrador",
  RECEPTIONIST: "Recepcionista",
};

export const ROLE_COLORS: Record<StaffRole, string> = {
  DOCTOR: "bg-purple-100 text-purple-700",
  NURSE: "bg-teal-100 text-teal-700",
  ADMIN: "bg-orange-100 text-orange-700",
  RECEPTIONIST: "bg-pink-100 text-pink-700",
};
