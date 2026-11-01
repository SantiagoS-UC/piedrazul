package co.edu.unicauca.piedrazul.appointments.infrastructure.export;

import co.edu.unicauca.piedrazul.appointments.application.AgendaEntry;
import co.edu.unicauca.piedrazul.appointments.application.AgendaExporter;
import co.edu.unicauca.piedrazul.appointments.application.ExportedFile;
import co.edu.unicauca.piedrazul.appointments.application.ProfessionalAgenda;
import co.edu.unicauca.piedrazul.identity.PatientSummary;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

/**
 * Agenda en CSV pensado para abrirse con doble clic en Excel configurado en español: separador
 * punto y coma (la coma es el separador decimal en Colombia) y BOM de UTF-8 para que las tildes y
 * la ñ se vean bien.
 */
@Component
class CsvAgendaExporter implements AgendaExporter {

    static final String CONTENT_TYPE = "text/csv; charset=UTF-8";
    private static final String SEPARATOR = ";";
    private static final String LINE_BREAK = "\r\n";
    private static final String BOM = "﻿";
    private static final List<String> HEADER = List.of("Fecha", "Hora", "Profesional", "Especialidad", "Paciente",
            "Tipo de documento", "Número de documento", "Teléfono");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public ExportedFile export(ProfessionalAgenda agenda) {
        String rows = agenda.entries().stream()
                .map(entry -> line(row(agenda, entry)))
                .collect(Collectors.joining());
        String csv = BOM + line(HEADER) + rows;
        return new ExportedFile(fileName(agenda), CONTENT_TYPE, csv.getBytes(StandardCharsets.UTF_8));
    }

    private static List<String> row(ProfessionalAgenda agenda, AgendaEntry entry) {
        String date = agenda.date().format(DATE);
        String time = entry.appointment().startTime().format(TIME);
        String professional = agenda.professional().fullName();
        String specialty = agenda.professional().specialtyName();
        return entry.patient()
                .map(patient -> List.of(date, time, professional, specialty, patientName(patient),
                        patient.documentType(), patient.documentNumber(), patient.phone()))
                .orElseGet(() -> List.of(date, time, professional, specialty, "Paciente no encontrado", "", "", ""));
    }

    private static String patientName(PatientSummary patient) {
        return patient.lastNames() + ", " + patient.givenNames();
    }

    private static String line(List<String> values) {
        return values.stream().map(CsvAgendaExporter::escape).collect(Collectors.joining(SEPARATOR)) + LINE_BREAK;
    }

    static String escape(String value) {
        String safe = neutralizeFormula(value);
        if (safe.contains(SEPARATOR) || safe.contains("\"") || safe.contains("\n") || safe.contains("\r")) {
            return "\"" + safe.replace("\"", "\"\"") + "\"";
        }
        return safe;
    }

    // Inyección de fórmulas (OWASP): Excel ejecuta como fórmula una celda que empieza por =, +, - o @.
    // Con un apóstrofo al inicio se muestra como texto.
    private static String neutralizeFormula(String value) {
        return !value.isEmpty() && "=+-@\t\r".indexOf(value.charAt(0)) >= 0 ? "'" + value : value;
    }

    // citas_laura-martinez_2026-11-03.csv: sin tildes ni espacios para evitar problemas al descargar.
    static String fileName(ProfessionalAgenda agenda) {
        String professional = Stream.of(agenda.professional().firstName(), agenda.professional().lastName())
                .map(CsvAgendaExporter::slug)
                .collect(Collectors.joining("-"));
        return "citas_" + professional + "_" + agenda.date() + ".csv";
    }

    private static String slug(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}
