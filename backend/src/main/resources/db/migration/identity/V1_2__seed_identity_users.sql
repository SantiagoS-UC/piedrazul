-- Usuarios iniciales para desarrollo, pruebas y la sustentación.
-- Todas las contraseñas siguen el formato <usuario>-1234, por ejemplo admin@gmail.com / admin-1234.

INSERT INTO users (id, email, password_hash, role, display_name, active, created_at) VALUES
    ('00000000-0000-0000-0000-000000000001', 'admin@gmail.com',
     '$2a$10$oP4NluFkAfWnu5dZLyvWguAn.8OMM3VxZW205Hvs.f/fioICPRvOW', 'ADMIN', 'Administrador Piedrazul', TRUE, NOW()),
    ('00000000-0000-0000-0000-000000000002', 'agendador@gmail.com',
     '$2a$10$H0BiFyxmd6B9Vp0Oxs1NUO5.VfjAeRBiD5kIldQZMfq3MnXErq5cW', 'SCHEDULER', 'Agendador Piedrazul', TRUE, NOW()),
    ('00000000-0000-0000-0000-000000000101', 'paciente1@gmail.com',
     '$2a$10$zpPtxRuMVKx72cK43xVX2.NHtHpsi8mFWDjpDJ6D4GB.GxJgdM.ue', 'PATIENT', 'Ana Gómez', TRUE, NOW()),
    ('00000000-0000-0000-0000-000000000102', 'paciente2@gmail.com',
     '$2a$10$CEENxU/P9Mp8sCB76GFwS.YtHqpwwYOVD4p6Fc5jtPJCRRY1cmhuy', 'PATIENT', 'Carlos Muñoz', TRUE, NOW());

INSERT INTO patients (id, first_name, middle_name, first_last_name, second_last_name, document_type,
                      document_number, birth_date, gender, phone) VALUES
    ('00000000-0000-0000-0000-000000000101', 'Ana', 'María', 'Gómez', 'Ruiz', 'CC',
     '1061000001', '1958-03-14', 'FEMALE', '3001234567'),
    ('00000000-0000-0000-0000-000000000102', 'Carlos', 'Andrés', 'Muñoz', 'Pérez', 'CC',
     '1061000002', '1962-11-02', 'MALE', '3107654321');
