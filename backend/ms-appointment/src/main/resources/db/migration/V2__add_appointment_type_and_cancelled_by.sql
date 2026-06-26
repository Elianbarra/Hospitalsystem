-- V2: Agrega appointment_type y cancelled_by (Task 14)
ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS appointment_type VARCHAR(20) NOT NULL DEFAULT 'CONSULTA',
    ADD COLUMN IF NOT EXISTS cancelled_by     VARCHAR(10);
