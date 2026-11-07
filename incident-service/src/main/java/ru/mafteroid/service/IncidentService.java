package ru.mafteroid.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mafteroid.entity.CheckResult;
import ru.mafteroid.entity.Incident;
import ru.mafteroid.entity.MonitorStatus;
import ru.mafteroid.events.CheckCompletedEvent;
import ru.mafteroid.repository.CheckResultRepository;
import ru.mafteroid.repository.IncidentRepository;
import ru.mafteroid.repository.MonitorStatusRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class IncidentService {
    private final IncidentRepository incidentRepository;
    private final CheckResultRepository checkResultRepository;
    private final MonitorStatusRepository monitorStatusRepository;

    public IncidentService(IncidentRepository incidentRepository, CheckResultRepository checkResultRepository, MonitorStatusRepository monitorStatusRepository) {
        this.incidentRepository = incidentRepository;
        this.checkResultRepository = checkResultRepository;
        this.monitorStatusRepository = monitorStatusRepository;
    }


    @Transactional
    public void process(CheckCompletedEvent event) {
        if (checkResultRepository.existsById(event.checkId())) {
            return;
        }

        CheckResult result = new CheckResult(event);
        checkResultRepository.save(result);
        updateStatus(result);
    }

    private void updateStatus(CheckResult result) {
        UUID monitorId = result.getMonitorId();
        OffsetDateTime checkedAt = result.getCompletedAt();
        String newStatus = result.getSuccessful() ? "UP" : "DOWN";

        MonitorStatus status = monitorStatusRepository.findById(monitorId)
                .orElseGet(() -> new MonitorStatus(monitorId));

        if (status.getLastCheckedAt() != null
                && checkedAt.isBefore(status.getLastCheckedAt())) {
            return;
        }

        if (!newStatus.equals(status.getStatus())) {
            updateIncident(monitorId, newStatus, checkedAt);
        }

        status.setStatus(newStatus);
        status.setLastCheckedAt(checkedAt);
        monitorStatusRepository.save(status);
    }

    private void updateIncident(
            UUID monitorId,
            String newStatus,
            OffsetDateTime checkedAt
    ) {
        var openIncident =
                incidentRepository.findByMonitorIdAndResolvedAtIsNull(monitorId);

        if ("DOWN".equals(newStatus) && openIncident.isEmpty()) {
            incidentRepository.save(new Incident(monitorId, checkedAt));
        } else if ("UP".equals(newStatus)) {
            openIncident.ifPresent(incident -> incident.setResolvedAt(checkedAt));
        }
    }
}
