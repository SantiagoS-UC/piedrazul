package co.edu.unicauca.piedrazul.configuration.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface SchedulingSettingsJpaRepository extends JpaRepository<SchedulingSettingsEntity, Integer> {
}
