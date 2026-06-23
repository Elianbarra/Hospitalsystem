-- seed_msauth_db.sql — credenciales para los 12 doctores
-- Contraseña de todos: Doctor123!
-- Ejecutar contra: msauth_db en NeonDB

INSERT INTO user_credentials (id, email, password, role, user_id, is_active, created_at) VALUES
  (gen_random_uuid(), 'andres.munoz@rednorte.cl',    '$2b$10$kIJhV7sKEeUS5gOKv8upBuyhkD7SzNHDUJ6LpA3/KVIkCCDq1qhTu', 'DOCTOR', 'd1a1b1c1-0001-0001-0001-000000000001', true, NOW()),
  (gen_random_uuid(), 'carmen.reyes@rednorte.cl',    '$2b$10$kIJhV7sKEeUS5gOKv8upBuyhkD7SzNHDUJ6LpA3/KVIkCCDq1qhTu', 'DOCTOR', 'd1a1b1c1-0001-0001-0001-000000000002', true, NOW()),
  (gen_random_uuid(), 'felipe.torres@rednorte.cl',   '$2b$10$kIJhV7sKEeUS5gOKv8upBuyhkD7SzNHDUJ6LpA3/KVIkCCDq1qhTu', 'DOCTOR', 'd1a1b1c1-0001-0001-0001-000000000003', true, NOW()),
  (gen_random_uuid(), 'valentina.soto@rednorte.cl',  '$2b$10$kIJhV7sKEeUS5gOKv8upBuyhkD7SzNHDUJ6LpA3/KVIkCCDq1qhTu', 'DOCTOR', 'd1a1b1c1-0001-0001-0001-000000000004', true, NOW()),
  (gen_random_uuid(), 'jorge.espinoza@rednorte.cl',  '$2b$10$kIJhV7sKEeUS5gOKv8upBuyhkD7SzNHDUJ6LpA3/KVIkCCDq1qhTu', 'DOCTOR', 'd1a1b1c1-0001-0001-0001-000000000005', true, NOW()),
  (gen_random_uuid(), 'daniela.fuentes@rednorte.cl', '$2b$10$kIJhV7sKEeUS5gOKv8upBuyhkD7SzNHDUJ6LpA3/KVIkCCDq1qhTu', 'DOCTOR', 'd1a1b1c1-0001-0001-0001-000000000006', true, NOW()),
  (gen_random_uuid(), 'rodrigo.castro@rednorte.cl',  '$2b$10$kIJhV7sKEeUS5gOKv8upBuyhkD7SzNHDUJ6LpA3/KVIkCCDq1qhTu', 'DOCTOR', 'd1a1b1c1-0001-0001-0001-000000000007', true, NOW()),
  (gen_random_uuid(), 'javiera.morales@rednorte.cl', '$2b$10$kIJhV7sKEeUS5gOKv8upBuyhkD7SzNHDUJ6LpA3/KVIkCCDq1qhTu', 'DOCTOR', 'd1a1b1c1-0001-0001-0001-000000000008', true, NOW()),
  (gen_random_uuid(), 'patricio.nunez@rednorte.cl',  '$2b$10$kIJhV7sKEeUS5gOKv8upBuyhkD7SzNHDUJ6LpA3/KVIkCCDq1qhTu', 'DOCTOR', 'd1a1b1c1-0001-0001-0001-000000000009', true, NOW()),
  (gen_random_uuid(), 'catalina.herrera@rednorte.cl','$2b$10$kIJhV7sKEeUS5gOKv8upBuyhkD7SzNHDUJ6LpA3/KVIkCCDq1qhTu', 'DOCTOR', 'd1a1b1c1-0001-0001-0001-000000000010', true, NOW()),
  (gen_random_uuid(), 'sebastian.diaz@rednorte.cl',  '$2b$10$kIJhV7sKEeUS5gOKv8upBuyhkD7SzNHDUJ6LpA3/KVIkCCDq1qhTu', 'DOCTOR', 'd1a1b1c1-0001-0001-0001-000000000011', true, NOW()),
  (gen_random_uuid(), 'isadora.vega@rednorte.cl',    '$2b$10$kIJhV7sKEeUS5gOKv8upBuyhkD7SzNHDUJ6LpA3/KVIkCCDq1qhTu', 'DOCTOR', 'd1a1b1c1-0001-0001-0001-000000000012', true, NOW());
