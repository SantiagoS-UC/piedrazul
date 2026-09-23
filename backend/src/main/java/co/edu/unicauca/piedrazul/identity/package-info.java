/**
 * Módulo Identity: Registro de pacientes, autenticación y roles (paciente, agendador, administrador).
 * <p>
 * Solo los tipos de este paquete raíz forman la API pública del módulo; los subpaquetes son
 * internos y Spring Modulith falla la verificación si otro módulo los usa directamente.
 */
@ApplicationModule(displayName = "Identity")
package co.edu.unicauca.piedrazul.identity;

import org.springframework.modulith.ApplicationModule;
