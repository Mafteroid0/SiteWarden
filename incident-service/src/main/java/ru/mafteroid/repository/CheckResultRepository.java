package ru.mafteroid.repository;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mafteroid.entity.CheckResult;

import java.util.List;
import java.util.UUID;

@Repository
public interface CheckResultRepository extends JpaRepository<CheckResult, UUID> {

    List<CheckResult> findByMonitorIdOrderByCompletedAtDesc(UUID monitorId, Limit limit);
}
