-- V2: Agrega campos nuevos y el valor OFFERED al enum waitlist_status

-- 1. Ampliar el enum de estado con OFFERED
ALTER TYPE waitlist_status ADD VALUE IF NOT EXISTS 'OFFERED';

-- 2. Agregar appointment_type (CONSULTA / CIRUGIA)
ALTER TABLE waitlist_entries
    ADD COLUMN IF NOT EXISTS appointment_type VARCHAR(20) NOT NULL DEFAULT 'CONSULTA';

-- 3. Agregar vital_risk
ALTER TABLE waitlist_entries
    ADD COLUMN IF NOT EXISTS vital_risk BOOLEAN NOT NULL DEFAULT FALSE;

-- 4. Agregar requeued_at (posición efectiva en cola; por defecto igual a created_at)
ALTER TABLE waitlist_entries
    ADD COLUMN IF NOT EXISTS requeued_at TIMESTAMP;

UPDATE waitlist_entries SET requeued_at = created_at WHERE requeued_at IS NULL;

ALTER TABLE waitlist_entries
    ALTER COLUMN requeued_at SET NOT NULL,
    ALTER COLUMN requeued_at SET DEFAULT NOW();
