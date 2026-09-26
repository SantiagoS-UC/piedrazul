-- Médicos y terapistas iniciales: dos por especialidad. La gestión de profesionales no está en el
-- alcance de esta iteración.

INSERT INTO professionals (id, first_name, last_name, specialty, active) VALUES
    ('20000000-0000-0000-0000-000000000001', 'Laura',    'Martínez',  'GENERAL_MEDICINE', TRUE),
    ('20000000-0000-0000-0000-000000000002', 'Andrés',   'Vargas',    'GENERAL_MEDICINE', TRUE),
    ('20000000-0000-0000-0000-000000000003', 'Diana',    'Rojas',     'NEURAL_THERAPY',   TRUE),
    ('20000000-0000-0000-0000-000000000004', 'Jorge',    'Castillo',  'NEURAL_THERAPY',   TRUE),
    ('20000000-0000-0000-0000-000000000005', 'Natalia',  'Guzmán',    'CHIROPRACTIC',     TRUE),
    ('20000000-0000-0000-0000-000000000006', 'Camilo',   'Benavides', 'CHIROPRACTIC',     TRUE),
    ('20000000-0000-0000-0000-000000000007', 'Paula',    'Zambrano',  'PHYSIOTHERAPY',    TRUE),
    ('20000000-0000-0000-0000-000000000008', 'Mauricio', 'López',     'PHYSIOTHERAPY',    TRUE);
