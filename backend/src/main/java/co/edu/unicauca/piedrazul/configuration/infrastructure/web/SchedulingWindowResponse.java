package co.edu.unicauca.piedrazul.configuration.infrastructure.web;

import co.edu.unicauca.piedrazul.configuration.domain.SchedulingWindow;

/**
 * Incluye los límites permitidos para que el formulario los muestre sin repetirlos en el frontend.
 */
public record SchedulingWindowResponse(int weeks, int minWeeks, int maxWeeks) {

    static SchedulingWindowResponse from(SchedulingWindow window) {
        return new SchedulingWindowResponse(window.weeks(), SchedulingWindow.MIN_WEEKS, SchedulingWindow.MAX_WEEKS);
    }
}
