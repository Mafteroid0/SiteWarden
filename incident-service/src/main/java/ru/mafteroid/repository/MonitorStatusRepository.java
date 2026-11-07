package ru.mafteroid.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mafteroid.entity.MonitorStatus;

import java.util.UUID;

public interface MonitorStatusRepository extends JpaRepository<MonitorStatus, UUID> {
}
