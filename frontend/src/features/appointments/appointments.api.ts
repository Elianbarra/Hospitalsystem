import { BFF_URL } from "@/lib/bff";
import type { Appointment, Doctor } from "./appointments.types";

export async function fetchPatientAppointments(
  patientId: string,
  token: string
): Promise<Appointment[]> {
  const res = await fetch(`${BFF_URL}/api/appointments/patient/${patientId}`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!res.ok) throw new Error("No se pudieron cargar las citas");
  return res.json();
}

export async function fetchDoctorAppointments(
  doctorId: string,
  token: string
): Promise<Appointment[]> {
  const res = await fetch(`${BFF_URL}/api/appointments/doctor/${doctorId}`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!res.ok) throw new Error("No se pudieron cargar las citas");
  return res.json();
}

export async function fetchDoctorsBySpecialty(
  specialty: string,
  token: string
): Promise<Doctor[]> {
  const res = await fetch(`${BFF_URL}/api/users/specialty/${specialty}`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!res.ok) return [];
  return res.json();
}

export interface CreateAppointmentPayload {
  patientId: string;
  doctorId: string;
  specialty: string;
  appointmentType: string;
  scheduledAt: string;
  notes?: string;
}

export async function createAppointment(
  payload: CreateAppointmentPayload,
  token: string
): Promise<void> {
  const res = await fetch(`${BFF_URL}/api/appointments`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    const d = await res.json().catch(() => ({})) as { message?: string };
    throw new Error(d.message ?? "Error al crear cita");
  }
}

export async function cancelAppointmentAsDoctor(
  appointmentId: string,
  token: string
): Promise<void> {
  const res = await fetch(
    `${BFF_URL}/api/reassignment/cancel-doctor/${appointmentId}`,
    { method: "PUT", headers: { Authorization: `Bearer ${token}` } }
  );
  if (!res.ok) throw new Error("No se pudo cancelar la cita");
}

export async function cancelAppointmentAsPatient(
  appointmentId: string,
  token: string
): Promise<void> {
  const res = await fetch(
    `${BFF_URL}/api/reassignment/cancel-patient/${appointmentId}`,
    { method: "PUT", headers: { Authorization: `Bearer ${token}` } }
  );
  if (!res.ok) throw new Error("No se pudo cancelar la cita");
}
