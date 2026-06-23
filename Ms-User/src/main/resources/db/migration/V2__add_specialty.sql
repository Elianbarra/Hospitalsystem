-- V2: Agrega columna specialty para médicos (MedicalSpecialty enum)
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS specialty VARCHAR(50);
