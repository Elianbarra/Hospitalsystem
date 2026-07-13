import { BFF_URL } from "@/lib/bff";
import type { WaitlistEntry } from "./waitlist.types";

export async function fetchAllWaitlist(token: string): Promise<WaitlistEntry[]> {
  const res = await fetch(`${BFF_URL}/api/waitlist`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!res.ok) throw new Error("No se pudo cargar la lista de espera");
  return res.json();
}

export async function fetchPatientWaitlist(
  patientId: string,
  token: string
): Promise<WaitlistEntry[]> {
  const res = await fetch(`${BFF_URL}/api/waitlist/patient/${patientId}`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!res.ok) throw new Error("No se pudo cargar la lista de espera");
  return res.json();
}

export async function fetchWaitlistBySpecialty(
  specialty: string,
  token: string
): Promise<WaitlistEntry[]> {
  const res = await fetch(`${BFF_URL}/api/waitlist/specialty/${specialty}`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!res.ok) return [];
  return res.json();
}

export interface JoinWaitlistPayload {
  patientId: string;
  specialty: string;
  appointmentType: string;
  vitalRisk: boolean;
  notes?: string;
}

export async function joinWaitlist(
  payload: JoinWaitlistPayload,
  token: string
): Promise<void> {
  const res = await fetch(`${BFF_URL}/api/waitlist`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    const d = await res.json().catch(() => ({})) as { message?: string };
    throw new Error(d.message ?? "Error al inscribirse");
  }
}

export async function cancelWaitlistEntry(
  entryId: string,
  token: string
): Promise<void> {
  const res = await fetch(`${BFF_URL}/api/waitlist/${entryId}/cancel`, {
    method: "PUT",
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!res.ok) throw new Error("No se pudo cancelar la entrada");
}

export async function updateVitalRisk(
  entryId: string,
  vitalRisk: boolean,
  token: string
): Promise<void> {
  const res = await fetch(`${BFF_URL}/api/waitlist/${entryId}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ vitalRisk }),
  });
  if (!res.ok) throw new Error("No se pudo actualizar el riesgo vital");
}
