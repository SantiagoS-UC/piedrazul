package co.edu.unicauca.piedrazul.configuration.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.unicauca.piedrazul.configuration.domain.SchedulingWindow;
import co.edu.unicauca.piedrazul.configuration.domain.SchedulingWindowRepository;
import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SchedulingWindowServiceTest {

    @Mock
    private SchedulingWindowRepository repository;

    private SchedulingWindowService service;

    @BeforeEach
    void setUp() {
        service = new SchedulingWindowService(repository);
    }

    @Test
    @DisplayName("Entrega la ventana vigente")
    void returnsCurrentWindow() {
        when(repository.current()).thenReturn(new SchedulingWindow(6));

        assertThat(service.current().weeks()).isEqualTo(6);
    }

    @Test
    @DisplayName("Guarda una ventana válida")
    void savesValidWindow() {
        SchedulingWindow updated = service.update(8);

        assertThat(updated.weeks()).isEqualTo(8);
        verify(repository).save(new SchedulingWindow(8));
    }

    @Test
    @DisplayName("No guarda una ventana fuera del rango")
    void rejectsInvalidWindow() {
        assertThatThrownBy(() -> service.update(20)).isInstanceOf(BusinessRuleViolationException.class);
        verify(repository, never()).save(any());
    }
}
