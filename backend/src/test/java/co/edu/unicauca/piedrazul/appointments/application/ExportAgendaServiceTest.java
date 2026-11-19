package co.edu.unicauca.piedrazul.appointments.application;

import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.LAURA;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.NEXT_WEDNESDAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.unicauca.piedrazul.appointments.domain.Appointment;
import co.edu.unicauca.piedrazul.shared.domain.AccessForbiddenException;
import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExportAgendaServiceTest {

    private static final Requester SCHEDULER = Requester.scheduler(UUID.randomUUID());
    private static final ExportedFile FILE = new ExportedFile("citas.csv", "text/csv", new byte[] {1});

    @Mock
    private AgendaQueryService agendaQueryService;
    @Mock
    private AgendaExporter agendaExporter;

    private ExportAgendaService service;

    @BeforeEach
    void setUp() {
        service = new ExportAgendaService(agendaQueryService, agendaExporter);
    }

    @Test
    @DisplayName("Exporta la agenda del día con el formato configurado")
    void exportsAgenda() {
        ProfessionalAgenda agenda = agendaWith(1);
        when(agendaQueryService.agendaOf(SCHEDULER, LAURA.id(), NEXT_WEDNESDAY)).thenReturn(agenda);
        when(agendaExporter.export(agenda)).thenReturn(FILE);

        assertThat(service.export(SCHEDULER, LAURA.id(), NEXT_WEDNESDAY)).isSameAs(FILE);
    }

    @Test
    @DisplayName("No genera un archivo vacío si no hay citas ese día")
    void rejectsEmptyAgenda() {
        when(agendaQueryService.agendaOf(SCHEDULER, LAURA.id(), NEXT_WEDNESDAY)).thenReturn(agendaWith(0));

        assertThatThrownBy(() -> service.export(SCHEDULER, LAURA.id(), NEXT_WEDNESDAY))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("No hay citas para exportar en esta fecha. Elige otra fecha.");
        verify(agendaExporter, never()).export(any());
    }

    @Test
    @DisplayName("Respeta la regla de que un profesional solo exporta su propia agenda")
    void propagatesAccessRule() {
        Requester other = Requester.professional(UUID.randomUUID());
        when(agendaQueryService.agendaOf(other, LAURA.id(), NEXT_WEDNESDAY))
                .thenThrow(new AccessForbiddenException("Solo puedes consultar tu propia agenda."));

        assertThatThrownBy(() -> service.export(other, LAURA.id(), NEXT_WEDNESDAY))
                .isInstanceOf(AccessForbiddenException.class);
        verify(agendaExporter, never()).export(any());
    }

    private static ProfessionalAgenda agendaWith(int appointments) {
        List<AgendaEntry> entries = IntStream.range(0, appointments)
                .mapToObj(i -> new AgendaEntry(Appointment.book(UUID.randomUUID(), UUID.randomUUID(), LAURA.id(),
                        NEXT_WEDNESDAY, LocalTime.of(8, 0).plusMinutes(20L * i), 20,
                        Instant.parse("2026-09-27T15:00:00Z")), Optional.empty()))
                .toList();
        return new ProfessionalAgenda(LAURA, NEXT_WEDNESDAY, entries);
    }
}
