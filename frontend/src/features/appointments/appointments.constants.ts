export const APPT_TO_WAITLIST_SPECIALTY: Record<string, string> = {
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

export const SPECIALTY_LABELS: Record<string, string> = {
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

export const APPOINTMENT_STATUS_COLORS: Record<string, string> = {
  PENDING: "bg-yellow-50 text-yellow-700",
  CONFIRMED: "bg-green-50 text-green-700",
  CANCELLED: "bg-red-50 text-red-500",
  COMPLETED: "bg-gray-50 text-gray-500",
};

export const PRIORITY_COLORS: Record<string, string> = {
  CRITICO: "bg-red-100 text-red-700",
  URGENTE: "bg-orange-100 text-orange-700",
  NORMAL: "bg-gray-100 text-gray-600",
};

export const WAITLIST_STATUS_COLORS: Record<string, string> = {
  WAITING: "bg-blue-50 text-blue-700",
  OFFERED: "bg-amber-50 text-amber-700",
  NOTIFIED: "bg-yellow-50 text-yellow-700",
  ASSIGNED: "bg-green-50 text-green-700",
  CANCELLED: "bg-red-50 text-red-500",
};
