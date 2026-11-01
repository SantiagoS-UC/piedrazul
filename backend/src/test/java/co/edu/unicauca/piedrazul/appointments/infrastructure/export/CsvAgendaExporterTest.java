package co.edu.unicauca.piedrazul.appointments.infrastructure.export;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unicauca.piedrazul.appointments.application.AgendaEntry;
import co.edu.unicauca.piedrazul.appointments.application.ExportedFile;
import co.edu.unicauca.piedrazul.appointments.application.ProfessionalAgenda;
import co.edu.unicauca.piedrazul.appointments.domain.Appointment;
import co.edu.unicauca.piedrazul.identity.PatientSummary;
import co.edu.unicauca.piedrazul.professionals.ProfessionalSummary;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CsvAgendaExporterTest {

    private static final ProfessionalSummary LAURA = new ProfessionalSummary(UUID.randomUUID(), "Laura", "Martínez",
            "GENERAL_MEDICINE", "Medicina general", true);
    private static final LocalDate DATE = LocalDate.of(2026, 11, 3);
    private static final PatientSummary ANA = new PatientSummary(UUID.randomUUID(), "Ana", "María", "Gómez", "Ruiz",
            "CC", "1061000001", "3001234567");

    private final CsvAgendaExporter exporter = new CsvAgendaExporter();

    @Test
    @DisplayName("Genera un CSV con encabezado y una fila por cita, separado por punto y coma")
    void writesHeaderAndRows() {
        ExportedFile file = exporter.export(agenda(entry(ANA, LocalTime.of(8, 0))));

        assertThat(lines(file)).containsExactly(
                "Fecha;Hora;Profesional;Especialidad;Paciente;Tipo de documento;Número de documento;Teléfono",
                "03/11/2026;08:00;Laura Martínez;Medicina general;Gómez Ruiz, Ana María;CC;1061000001;3001234567");
        assertThat(file.contentType()).isEqualTo("text/csv; charset=UTF-8");
    }

    @Test
    @DisplayName("Empieza con el BOM de UTF-8 para que Excel muestre bien las tildes")
    void startsWithBom() {
        byte[] content = exporter.export(agenda(entry(ANA, LocalTime.of(8, 0)))).content();

        assertThat(content).startsWith((byte) 0xEF, (byte) 0xBB, (byte) 0xBF);
    }

    @Test
    @DisplayName("Usa un nombre de archivo sin tildes con el profesional y la fecha")
    void namesFileByProfessionalAndDate() {
        assertThat(exporter.export(agenda()).fileName()).isEqualTo("citas_laura-martinez_2026-11-03.csv");
    }

    @Test
    @DisplayName("Mantiene la fila aunque no se encuentren los datos del paciente")
    void keepsRowWithoutPatient() {
        AgendaEntry orphan = new AgendaEntry(appointment(LocalTime.of(9, 0)), Optional.empty());

        assertThat(lines(exporter.export(agenda(orphan))).get(1))
                .isEqualTo("03/11/2026;09:00;Laura Martínez;Medicina general;Paciente no encontrado;;;");
    }

    @Test
    @DisplayName("Encierra entre comillas los valores con punto y coma o comillas")
    void quotesSpecialCharacters() {
        assertThat(CsvAgendaExporter.escape("Pérez; Ana")).isEqualTo("\"Pérez; Ana\"");
        assertThat(CsvAgendaExporter.escape("Ana \"la Mona\"")).isEqualTo("\"Ana \"\"la Mona\"\"\"");
    }

    @Test
    @DisplayName("Neutraliza valores que Excel interpretaría como fórmulas")
    void neutralizesFormulas() {
        assertThat(CsvAgendaExporter.escape("=HYPERLINK(\"http://x\")")).startsWith("\"'=HYPERLINK");
        assertThat(CsvAgendaExporter.escape("+57300")).isEqualTo("'+57300");
        assertThat(CsvAgendaExporter.escape("@SUM(A1)")).isEqualTo("'@SUM(A1)");
    }

    private static List<String> lines(ExportedFile file) {
        String text = new String(file.content(), StandardCharsets.UTF_8).substring(1);
        return List.of(text.split("\r\n"));
    }

    private static ProfessionalAgenda agenda(AgendaEntry... entries) {
        return new ProfessionalAgenda(LAURA, DATE, List.of(entries));
    }

    private static AgendaEntry entry(PatientSummary patient, LocalTime time) {
        return new AgendaEntry(appointment(time), Optional.of(patient));
    }

    private static Appointment appointment(LocalTime time) {
        return Appointment.book(UUID.randomUUID(), ANA.id(), LAURA.id(), DATE, time, 20,
                Instant.parse("2026-11-01T15:00:00Z"));
    }
}
