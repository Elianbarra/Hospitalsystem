INSERT INTO user_credentials (id, email, password, role, user_id, is_active, created_at)
VALUES (gen_random_uuid(), 'admin@rednorte.cl',
  '$2b$10$4KhfSswgBZKShlC1bUXh4eOzNxisP4q9eGWltrscR/Wu0XoAtDUb.',
  'ADMIN', '708a1cd6-8447-4338-b636-efa3c63391f0', TRUE, NOW())
ON CONFLICT (email) DO NOTHING;
