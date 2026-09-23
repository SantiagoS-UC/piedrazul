/**
 * Módulo Appointments: Cálculo de franjas disponibles, agendamiento y consulta de citas por profesional y fecha.
 * <p>
 * Solo los tipos de este paquete raíz forman la API pública del módulo; los subpaquetes son
 * internos y Spring Modulith falla la verificación si otro módulo los usa directamente.
 */
@ApplicationModule(displayName = "Appointments")
package co.edu.unicauca.piedrazul.appointments;

import org.springframework.modulith.ApplicationModule;
