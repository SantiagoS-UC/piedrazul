/**
 * Módulo Configuration: Parámetros de agendamiento: ventana en semanas, días de atención, franja horaria e intervalo entre citas de cada profesional.
 * <p>
 * Solo los tipos de este paquete raíz forman la API pública del módulo; los subpaquetes son
 * internos y Spring Modulith falla la verificación si otro módulo los usa directamente.
 */
@ApplicationModule(displayName = "Configuration")
package co.edu.unicauca.piedrazul.configuration;

import org.springframework.modulith.ApplicationModule;
