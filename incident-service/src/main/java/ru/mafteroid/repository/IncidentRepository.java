package ru.mafteroid.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mafteroid.entity.CheckResult;

import java.util.UUID;

@Repository
public interface IncidentRepository extends JpaRepository<CheckResult, UUID> {

}
