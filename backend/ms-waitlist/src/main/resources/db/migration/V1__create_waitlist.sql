CREATE TYPE waitlist_status AS ENUM ('WAITING', 'NOTIFIED', 'ASSIGNED', 'CANCELLED');
CREATE TYPE waitlist_priority AS ENUM ('NORMAL', 'URGENTE', 'CRITICO');
CREATE TYPE waitlist_specialty AS ENUM (
    'MEDICINA_GENERAL',
    'PEDIATRIA',
    'CARDIOLOGIA',
    'TRAUMATOLOGIA',
    'NEUROLOGIA',
    'GINECOLOGIA',
    'OFTALMOLOGIA',
    'DERMATOLOGIA',
    'PSIQUIATRIA',
    'ONCOLOGIA'
);

CREATE TABLE waitlist_entries (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id      UUID        NOT NULL,
    specialty       waitlist_specialty NOT NULL,
    priority        waitlist_priority  NOT NULL DEFAULT 'NORMAL',
    status          waitlist_status    NOT NULL DEFAULT 'WAITING',
    notes           TEXT,
    active          BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_waitlist_patient   ON waitlist_entries(patient_id);
CREATE INDEX idx_waitlist_specialty ON waitlist_entries(specialty);
CREATE INDEX idx_waitlist_status    ON waitlist_entries(status);
CREATE INDEX idx_waitlist_priority  ON waitlist_entries(priority, created_at);
