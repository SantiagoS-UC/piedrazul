-- Las cuentas de acceso (correo, contraseña y rol) pasan a Keycloak, que conserva los mismos ids.
-- patients.id sigue siendo el id de la cuenta, ahora el claim sub de los tokens de Keycloak.

ALTER TABLE patients DROP CONSTRAINT patients_id_fkey;
DROP TABLE users;
