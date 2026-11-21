-- RF7: el reporte mensual busca las citas de un año completo por fecha.
CREATE INDEX ix_appointments_date ON appointments (appointment_date);
