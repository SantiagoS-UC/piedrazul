package co.edu.unicauca.piedrazul.configuration.infrastructure.persistence;

import co.edu.unicauca.piedrazul.configuration.domain.SchedulingWindow;
import co.edu.unicauca.piedrazul.configuration.domain.SchedulingWindowRepository;
import org.springframework.stereotype.Repository;

@Repository
class SchedulingWindowRepositoryAdapter implements SchedulingWindowRepository {

    private final SchedulingSettingsJpaRepository jpaRepository;

    SchedulingWindowRepositoryAdapter(SchedulingSettingsJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SchedulingWindow current() {
        return jpaRepository.findById(SchedulingSettingsEntity.SINGLETON_ID)
                .map(entity -> new SchedulingWindow(entity.windowWeeks()))
                .orElse(SchedulingWindow.DEFAULT);
    }

    @Override
    public void save(SchedulingWindow window) {
        jpaRepository.save(new SchedulingSettingsEntity(window.weeks()));
    }
}
