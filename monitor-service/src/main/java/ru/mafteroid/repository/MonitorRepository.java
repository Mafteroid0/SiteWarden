package ru.mafteroid.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mafteroid.entity.Monitor;

import java.util.UUID;

public interface MonitorRepository extends JpaRepository<Monitor, UUID> {
}
