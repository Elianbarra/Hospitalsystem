"use client";
import { useState, useCallback } from "react";
import type { WaitlistEntry } from "./waitlist.types";
import { APPT_TO_WAITLIST_SPECIALTY } from "@/features/appointments/appointments.constants";
import {
  fetchAllWaitlist,
  fetchPatientWaitlist,
  joinWaitlist,
  cancelWaitlistEntry,
  updateVitalRisk,
} from "./waitlist.api";

interface Session {
  userId: string;
  role: string;
  token: string;
}

export function useWaitlist({ userId, role, token }: Session) {
  const [waitlist, setWaitlist] = useState<WaitlistEntry[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [showForm, setShowForm] = useState(false);
  const [specialty, setSpecialty] = useState("");
  const [type, setType] = useState<"CONSULTA" | "CIRUGIA">("CONSULTA");
  const [vitalRisk, setVitalRisk] = useState(false);
  const [notes, setNotes] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const load = useCallback(() => {
    setLoading(true);
    setError("");
    const fetcher =
      role === "DOCTOR" || role === "ADMIN"
        ? () => fetchAllWaitlist(token)
        : () => fetchPatientWaitlist(userId, token);
    fetcher()
      .then(setWaitlist)
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoading(false));
  }, [userId, role, token]);

  const toggleForm = useCallback(() => {
    setShowForm((prev) => !prev);
    setSuccess("");
    setError("");
  }, []);

  const handleJoin = useCallback(
    async (e: React.FormEvent) => {
      e.preventDefault();
      if (!specialty) return;
      setSubmitting(true);
      setError("");
      try {
        await joinWaitlist(
          {
            patientId: userId,
            specialty: APPT_TO_WAITLIST_SPECIALTY[specialty] ?? specialty,
            appointmentType: type,
            vitalRisk,
            notes: notes || undefined,
          },
          token
        );
        setSuccess("Inscrito en lista de espera");
        setShowForm(false);
        setSpecialty("");
        setType("CONSULTA");
        setVitalRisk(false);
        setNotes("");
        load();
      } catch (err: unknown) {
        setError(err instanceof Error ? err.message : "Error inesperado");
      } finally {
        setSubmitting(false);
      }
    },
    [userId, specialty, type, vitalRisk, notes, token, load]
  );

  const handleCancel = useCallback(
    async (entryId: string) => {
      try {
        await cancelWaitlistEntry(entryId, token);
        load();
      } catch {
        setError("No se pudo cancelar la entrada");
      }
    },
    [token, load]
  );

  const handleUpdateVitalRisk = useCallback(
    async (entryId: string, newVitalRisk: boolean) => {
      try {
        await updateVitalRisk(entryId, newVitalRisk, token);
        load();
      } catch {
        setError("No se pudo actualizar el riesgo vital");
      }
    },
    [token, load]
  );

  return {
    waitlist,
    loading,
    error,
    success,
    showForm,
    specialty,
    type,
    vitalRisk,
    notes,
    submitting,
    load,
    toggleForm,
    setSpecialty,
    setType,
    setVitalRisk,
    setNotes,
    handleJoin,
    handleCancel,
    handleUpdateVitalRisk,
  };
}
