package co.edu.unicauca.piedrazul.shared.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Monitoreo de logs: una línea por cada petición a la API con el método, la ruta, el estado y el
 * tiempo de respuesta. Cada petición lleva un identificador (X-Request-Id) que aparece en todas sus
 * líneas de log y se devuelve al cliente, para encontrar en Render lo que pasó con una petición.
 *
 * <p>No se registran datos personales: ni el usuario, ni el cuerpo, ni los parámetros de la URL
 * (pueden llevar documentos de identidad).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RequestLoggingFilter extends OncePerRequestFilter {

    static final String REQUEST_ID_HEADER = "X-Request-Id";
    static final String REQUEST_ID_KEY = "requestId";

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    // Se acepta el id que mande el cliente solo si es corto y simple, para no ensuciar los logs.
    private static final Pattern SAFE_ID = Pattern.compile("[A-Za-z0-9-]{1,64}");
    private static final long SLOW_REQUEST_MS = 2000;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestId = requestIdOf(request);
        MDC.put(REQUEST_ID_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        long start = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            if (request.getRequestURI().startsWith("/api/")) {
                logRequest(request, response.getStatus(), (System.nanoTime() - start) / 1_000_000);
            }
            MDC.remove(REQUEST_ID_KEY);
        }
    }

    private static void logRequest(HttpServletRequest request, int status, long millis) {
        String line = "%s %s -> %d (%d ms)".formatted(request.getMethod(), request.getRequestURI(), status, millis);
        if (status >= 500) {
            log.error(line);
        } else if (millis >= SLOW_REQUEST_MS) {
            log.warn("{} lenta", line);
        } else {
            log.info(line);
        }
    }

    private static String requestIdOf(HttpServletRequest request) {
        String received = request.getHeader(REQUEST_ID_HEADER);
        return received != null && SAFE_ID.matcher(received).matches() ? received : UUID.randomUUID().toString();
    }
}
