export interface Doctor {
  id: string;
  firstName: string;
  lastName: string;
  specialty: string;
}

export interface Appointment {
  id: string;
  patientId: string;
  doctorId: string;
  specialty: string;
  appointmentType: string;
  scheduledAt: string;
  status: string;
  cancelledBy?: string;
  notes?: string;
}
