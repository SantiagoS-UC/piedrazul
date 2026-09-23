/**
 * Kernel compartido: excepciones y tipos comunes a todos los módulos. Es un módulo abierto,
 * así que debe mantenerse pequeño y sin lógica de negocio de ningún módulo en particular.
 */
@ApplicationModule(displayName = "Shared", type = ApplicationModule.Type.OPEN)
package co.edu.unicauca.piedrazul.shared;

import org.springframework.modulith.ApplicationModule;
