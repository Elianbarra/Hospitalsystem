-- V3: Convierte tipos enum nativos de PostgreSQL a VARCHAR
-- Hibernate mapea enums Java como VARCHAR; PostgreSQL rechazaba el INSERT
-- porque las columnas estaban tipadas como custom enum types.
-- Hay que quitar los DEFAULT que referencian los tipos enum ANTES de dropearlos.

-- 1. Quitar defaults que dependen de los enum types
ALTER TABLE waitlist_entries ALTER COLUMN priority DROP DEFAULT;
ALTER TABLE waitlist_entries ALTER COLUMN status   DROP DEFAULT;

-- 2. Convertir columnas a VARCHAR
ALTER TABLE waitlist_entries ALTER COLUMN priority  TYPE VARCHAR(20) USING priority::VARCHAR;
ALTER TABLE waitlist_entries ALTER COLUMN status    TYPE VARCHAR(30) USING status::VARCHAR;
ALTER TABLE waitlist_entries ALTER COLUMN specialty TYPE VARCHAR(50) USING specialty::VARCHAR;

-- 3. Restaurar defaults como strings simples
ALTER TABLE waitlist_entries ALTER COLUMN priority SET DEFAULT 'NORMAL';
ALTER TABLE waitlist_entries ALTER COLUMN status   SET DEFAULT 'WAITING';

-- 4. Eliminar los tipos enum ya sin dependencias
DROP TYPE IF EXISTS waitlist_priority;
DROP TYPE IF EXISTS waitlist_status;
DROP TYPE IF EXISTS waitlist_specialty;
