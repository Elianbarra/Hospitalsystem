"use client";
import { useState, useCallback } from "react";
import type { Appointment, Doctor } from "./appointments.types";
import { APPT_TO_WAITLIST_SPECIALTY } from "./appointments.constants";
import {
  fetchPatientAppointments,
  fetchDoctorAppointments,
  fetchDoctorsBySpecialty,
  createAppointment,
  cancelAppointmentAsDoctor,
  cancelAppointmentAsPatient,
} from "./appointments.api";
import { findAvailableSlots } from "./appointments.utils";
import { fetchWaitlistBySpecialty } from "@/features/waitlist/waitlist.api";

interface Session {
  userId: string;
  role: string;
  token: string;
}

export function useAppointments({ userId, role, token }: Session) {
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [showForm, setShowForm] = useState(false);
  const [specialty, setSpecialty] = useState("");
  const [doctors, setDoctors] = useState<Doctor[]>([]);
  const [doctorId, setDoctorId] = useState("");
  const [availableSlots, setAvailableSlots] = useState<string[]>([]);
  const [selectedSlot, setSelectedSlot] = useState<string | null>(null);
  const [slotsLoading, setSlotsLoading] = useState(false);
  const [waitlistPosition, setWaitlistPosition] = useState<number | null>(null);
  const [type, setType] = useState<"CONSULTA" | "CIRUGIA">("CONSULTA");
  const [notes, setNotes] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const load = useCallback(() => {
    setLoading(true);
    setError("");
    const fetcher =
      role === "DOCTOR"
        ? () => fetchDoctorAppointments(userId, token)
        : () => fetchPatientAppointments(userId, token);
    fetcher()
      .then(setAppointments)
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoading(false));
  }, [userId, role, token]);

  const toggleForm = useCallback(() => {
    setShowForm((prev) => !prev);
    setSuccess("");
    setError("");
  }, []);

  const onSpecialtyChange = useCallback(
    async (newSpecialty: string) => {
      setSpecialty(newSpecialty);
      setDoctorId("");
      setAvailableSlots([]);
      setSelectedSlot(null);
      setWaitlistPosition(null);
      setDoctors([]);
      if (!newSpecialty) return;
      try {
        const [docs, wlList] = await Promise.all([
          fetchDoctorsBySpecialty(newSpecialty, token),
          fetchWaitlistBySpecialty(
            APPT_TO_WAITLIST_SPECIALTY[newSpecialty] ?? newSpecialty,
            token
          ),
        ]);
        setDoctors(docs);
        const pos = wlList.findIndex(
          (e) => e.patientId === userId && e.status === "WAITING"
        );
        setWaitlistPosition(pos >= 0 ? pos + 1 : null);
      } catch {
        /* silencioso */
      }
    },
    [userId, token]
  );

  const onDoctorChange = useCallback(
    async (newDoctorId: string) => {
      setDoctorId(newDoctorId);
      setAvailableSlots([]);
      setSelectedSlot(null);
      if (!newDoctorId) return;
      setSlotsLoading(true);
      try {
        const appts = await fetchDoctorAppointments(newDoctorId, token);
        setAvailableSlots(findAvailableSlots(appts));
      } catch {
        /* silencioso */
      } finally {
        setSlotsLoading(false);
      }
    },
    [token]
  );

  const handleCreate = useCallback(
    async (e: React.FormEvent) => {
      e.preventDefault();
      if (!doctorId || !selectedSlot || !specialty) return;
      setSubmitting(true);
      setError("");
      try {
        await createAppointment(
          {
            patientId: userId,
            doctorId,
            specialty,
            appointmentType: type,
            scheduledAt: selectedSlot,
            notes: notes || undefined,
          },
          token
        );
        setSuccess("Cita creada exitosamente");
        setShowForm(false);
        setSpecialty("");
        setDoctorId("");
        setAvailableSlots([]);
        setSelectedSlot(null);
        setNotes("");
        setType("CONSULTA");
        load();
      } catch (err: unknown) {
        setError(err instanceof Error ? err.message : "Error inesperado");
      } finally {
        setSubmitting(false);
      }
    },
    [doctorId, selectedSlot, specialty, userId, type, notes, token, load]
  );

  const handleCancel = useCallback(
    async (appt: Appointment) => {
      try {
        if (role === "DOCTOR") {
          await cancelAppointmentAsDoctor(appt.id, token);
        } else {
          await cancelAppointmentAsPatient(appt.id, token);
        }
        load();
      } catch (err: unknown) {
        setError(err instanceof Error ? err.message : "Error al cancelar");
      }
    },
    [role, token, load]
  );

  return {
    appointments,
    loading,
    error,
    success,
    showForm,
    specialty,
    doctors,
    doctorId,
    availableSlots,
    selectedSlot,
    slotsLoading,
    waitlistPosition,
    type,
    notes,
    submitting,
    load,
    toggleForm,
    onSpecialtyChange,
    onDoctorChange,
    setSelectedSlot,
    setType,
    setNotes,
    handleCreate,
    handleCancel,
  };
}
