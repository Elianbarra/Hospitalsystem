INSERT INTO users (id, first_name, last_name, email, phone, document_type,
  document_number, role, specialty, is_active, created_at, updated_at)
VALUES ('708a1cd6-8447-4338-b636-efa3c63391f0', 'Admin', 'Sistema',
  'admin@rednorte.cl', '+56912345678', 'RUT', '11111111-1', 'ADMIN',
  NULL, TRUE, NOW(), NOW())
ON CONFLICT (email) DO NOTHING;
