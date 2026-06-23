-- seed_msuser_db.sql — 12 doctores (4 por especialidad: CARDIOLOGY, NEUROLOGY, GENERAL)
-- Ejecutar contra: msuser_db en NeonDB

INSERT INTO users (id, first_name, last_name, email, phone, document_type, document_number, role, specialty, is_active, created_at, updated_at) VALUES
  ('d1a1b1c1-0001-0001-0001-000000000001', 'Andrés',    'Muñoz',    'andres.munoz@rednorte.cl',    '912345001', 'RUT', '912345001-K', 'DOCTOR', 'CARDIOLOGY', true, NOW(), NOW()),
  ('d1a1b1c1-0001-0001-0001-000000000002', 'Carmen',    'Reyes',    'carmen.reyes@rednorte.cl',    '912345002', 'RUT', '912345002-K', 'DOCTOR', 'CARDIOLOGY', true, NOW(), NOW()),
  ('d1a1b1c1-0001-0001-0001-000000000003', 'Felipe',    'Torres',   'felipe.torres@rednorte.cl',   '912345003', 'RUT', '912345003-K', 'DOCTOR', 'CARDIOLOGY', true, NOW(), NOW()),
  ('d1a1b1c1-0001-0001-0001-000000000004', 'Valentina', 'Soto',     'valentina.soto@rednorte.cl',  '912345004', 'RUT', '912345004-K', 'DOCTOR', 'CARDIOLOGY', true, NOW(), NOW()),
  ('d1a1b1c1-0001-0001-0001-000000000005', 'Jorge',     'Espinoza', 'jorge.espinoza@rednorte.cl',  '912345005', 'RUT', '912345005-K', 'DOCTOR', 'NEUROLOGY',  true, NOW(), NOW()),
  ('d1a1b1c1-0001-0001-0001-000000000006', 'Daniela',   'Fuentes',  'daniela.fuentes@rednorte.cl', '912345006', 'RUT', '912345006-K', 'DOCTOR', 'NEUROLOGY',  true, NOW(), NOW()),
  ('d1a1b1c1-0001-0001-0001-000000000007', 'Rodrigo',   'Castro',   'rodrigo.castro@rednorte.cl',  '912345007', 'RUT', '912345007-K', 'DOCTOR', 'NEUROLOGY',  true, NOW(), NOW()),
  ('d1a1b1c1-0001-0001-0001-000000000008', 'Javiera',   'Morales',  'javiera.morales@rednorte.cl', '912345008', 'RUT', '912345008-K', 'DOCTOR', 'NEUROLOGY',  true, NOW(), NOW()),
  ('d1a1b1c1-0001-0001-0001-000000000009', 'Patricio',  'Núñez',    'patricio.nunez@rednorte.cl',  '912345009', 'RUT', '912345009-K', 'DOCTOR', 'GENERAL',    true, NOW(), NOW()),
  ('d1a1b1c1-0001-0001-0001-000000000010', 'Catalina',  'Herrera',  'catalina.herrera@rednorte.cl','912345010', 'RUT', '912345010-K', 'DOCTOR', 'GENERAL',    true, NOW(), NOW()),
  ('d1a1b1c1-0001-0001-0001-000000000011', 'Sebastián', 'Díaz',     'sebastian.diaz@rednorte.cl',  '912345011', 'RUT', '912345011-K', 'DOCTOR', 'GENERAL',    true, NOW(), NOW()),
  ('d1a1b1c1-0001-0001-0001-000000000012', 'Isadora',   'Vega',     'isadora.vega@rednorte.cl',    '912345012', 'RUT', '912345012-K', 'DOCTOR', 'GENERAL',    true, NOW(), NOW());
