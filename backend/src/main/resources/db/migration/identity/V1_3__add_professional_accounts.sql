-- Cuentas para los médicos y terapistas, que en esta iteración exportan sus citas y consultan el
-- historial de los pacientes. El id de cada cuenta es el mismo del profesional en la tabla
-- professionals, igual que el id de la cuenta de un paciente es el de su registro en patients.
-- Las contraseñas siguen el formato <usuario>-1234, por ejemplo laura.martinez@gmail.com / laura.martinez-1234.

ALTER TABLE users DROP CONSTRAINT users_role_check;
ALTER TABLE users ADD CONSTRAINT users_role_check
    CHECK (role IN ('PATIENT', 'SCHEDULER', 'PROFESSIONAL', 'ADMIN'));

INSERT INTO users (id, email, password_hash, role, display_name, active, created_at) VALUES
    ('20000000-0000-0000-0000-000000000001', 'laura.martinez@gmail.com',
     '$2a$10$h62EGmmgAR9Lw6in2ZyuO.EHmlgx7OxrkTTB2Xtv3oK57UmKeqd4y', 'PROFESSIONAL', 'Laura Martínez', TRUE, NOW()),
    ('20000000-0000-0000-0000-000000000002', 'andres.vargas@gmail.com',
     '$2a$10$QzGHbZDQ.yFkkJgiY3PRp.78xHJL7HXrzRT4BH0hIUxYMwRWpRm4G', 'PROFESSIONAL', 'Andrés Vargas', TRUE, NOW()),
    ('20000000-0000-0000-0000-000000000003', 'diana.rojas@gmail.com',
     '$2a$10$50.MhVN5wemzz6ydK7nkcOpqVbK2bi1Qie.Lua63AOc5pCKt0xNhe', 'PROFESSIONAL', 'Diana Rojas', TRUE, NOW()),
    ('20000000-0000-0000-0000-000000000004', 'jorge.castillo@gmail.com',
     '$2a$10$I/7rANyi0sY.ik0fCLOeNuQ2zHwv9K1F.Gs/0XLL//iIKiea22PPm', 'PROFESSIONAL', 'Jorge Castillo', TRUE, NOW()),
    ('20000000-0000-0000-0000-000000000005', 'natalia.guzman@gmail.com',
     '$2a$10$He7xPyY2Pu0UlIstTen3m.bsmFiJF8xFKJkgTxqiKtIoiwB/uLK3.', 'PROFESSIONAL', 'Natalia Guzmán', TRUE, NOW()),
    ('20000000-0000-0000-0000-000000000006', 'camilo.benavides@gmail.com',
     '$2a$10$H6.43SSic5kqezTEpPRc7eabbkxMbxnzpJn4i.zCavp.cPL.DCizS', 'PROFESSIONAL', 'Camilo Benavides', TRUE, NOW()),
    ('20000000-0000-0000-0000-000000000007', 'paula.zambrano@gmail.com',
     '$2a$10$Zi3um.6HdIs.HjaRPGQadu9MUAaRd2uNQCS9L8vniyK5iuYdeSGGi', 'PROFESSIONAL', 'Paula Zambrano', TRUE, NOW()),
    ('20000000-0000-0000-0000-000000000008', 'mauricio.lopez@gmail.com',
     '$2a$10$4THfsQYEgrBw.kgbPehNY.SaULbjJMEyzwPZK/rh2.qHCLgpBCiv.', 'PROFESSIONAL', 'Mauricio López', TRUE, NOW());
