export interface WaitlistEntry {
  id: string;
  patientId: string;
  specialty: string;
  appointmentType: string;
  priority: string;
  vitalRisk: boolean;
  status: string;
  notes?: string;
  createdAt: string;
  requeuedAt: string;
}
