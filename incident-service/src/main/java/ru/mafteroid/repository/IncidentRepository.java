package ru.mafteroid.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mafteroid.entity.Incident;

import java.util.Optional;
import java.util.UUID;

public interface IncidentRepository extends JpaRepository<Incident, UUID> {
    Optional<Incident> findByMonitorIdAndResolvedAtIsNull(UUID id);
}
