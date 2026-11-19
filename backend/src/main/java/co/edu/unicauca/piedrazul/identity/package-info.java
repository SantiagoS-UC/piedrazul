/**
 * Módulo Identity: registro de pacientes y búsqueda por documento. Las cuentas, contraseñas y roles
 * viven en Keycloak; este módulo valida sus tokens y crea allí la cuenta de cada paciente.
 * <p>
 * Solo los tipos de este paquete raíz forman la API pública del módulo; los subpaquetes son
 * internos y Spring Modulith falla la verificación si otro módulo los usa directamente.
 */
@ApplicationModule(displayName = "Identity")
package co.edu.unicauca.piedrazul.identity;

import org.springframework.modulith.ApplicationModule;
