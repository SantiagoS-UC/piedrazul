-- Configuración inicial: ventana de 4 semanas y disponibilidad para seis de los ocho profesionales.
-- Jorge Castillo y Mauricio López quedan sin configurar, para mostrar que un profesional sin
-- disponibilidad no aparece al agendar.

INSERT INTO scheduling_settings (id, window_weeks) VALUES (1, 4);

INSERT INTO professional_availabilities (professional_id, working_days, start_time, end_time, slot_minutes) VALUES
    ('20000000-0000-0000-0000-000000000001', 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY', '08:00', '12:00', 20),
    ('20000000-0000-0000-0000-000000000002', 'MONDAY,WEDNESDAY,FRIDAY',                  '14:00', '18:00', 30),
    ('20000000-0000-0000-0000-000000000003', 'TUESDAY,THURSDAY',                         '08:00', '12:00', 40),
    ('20000000-0000-0000-0000-000000000005', 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY', '08:00', '11:00', 30),
    ('20000000-0000-0000-0000-000000000006', 'TUESDAY,THURSDAY,SATURDAY',                '09:00', '13:00', 45),
    ('20000000-0000-0000-0000-000000000007', 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY', '13:00', '17:00', 60);
