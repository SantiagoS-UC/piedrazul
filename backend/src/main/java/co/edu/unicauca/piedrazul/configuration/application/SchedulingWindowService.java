package co.edu.unicauca.piedrazul.configuration.application;

import co.edu.unicauca.piedrazul.configuration.domain.SchedulingWindow;
import co.edu.unicauca.piedrazul.configuration.domain.SchedulingWindowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-05: ventana de tiempo en la que se habilitan las citas.
 */
@Service
public class SchedulingWindowService {

    private final SchedulingWindowRepository repository;

    public SchedulingWindowService(SchedulingWindowRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public SchedulingWindow current() {
        return repository.current();
    }

    @Transactional
    public SchedulingWindow update(int weeks) {
        SchedulingWindow window = new SchedulingWindow(weeks);
        repository.save(window);
        return window;
    }
}
