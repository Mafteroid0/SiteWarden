package ru.mafteroid.service;

import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import ru.mafteroid.MonitorStatusResponse;
import ru.mafteroid.entity.CheckResult;
import ru.mafteroid.entity.Incident;
import ru.mafteroid.entity.MonitorStatus;
import ru.mafteroid.exceptions.InvalidRangeException;
import ru.mafteroid.repository.CheckResultRepository;
import ru.mafteroid.repository.IncidentRepository;
import ru.mafteroid.repository.MonitorStatusRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ApiService {

    private final MonitorStatusRepository monitorStatusRepository;
    private final CheckResultRepository checkResultRepository;
    private final IncidentRepository incidentRepository;

    public ApiService(MonitorStatusRepository monitorStatusRepository, CheckResultRepository checkResultRepository, IncidentRepository incidentRepository) {
        this.monitorStatusRepository = monitorStatusRepository;
        this.checkResultRepository = checkResultRepository;
        this.incidentRepository = incidentRepository;
    }

    public MonitorStatusResponse findById(UUID monitorId){
        MonitorStatus monitorStatus = monitorStatusRepository.findById(monitorId).orElse(null);
        String status = "UNKNOWN";
        OffsetDateTime lastChecked = null;

        if (monitorStatus != null) {
            status = monitorStatus.getStatus();
            lastChecked = monitorStatus.getLastCheckedAt();

        }
        return new MonitorStatusResponse(monitorId, status, lastChecked);

    }

    public List<CheckResult> getChecks(UUID id, Integer limit){
        if (limit == null){
            limit = 10;
        }
        if (limit < 1 || limit > 100){
            throw new InvalidRangeException(limit.toString());
        }
        return checkResultRepository.findByMonitorIdOrderByCompletedAtDesc(id, Limit.of(limit));
    }

    public List<Incident> getIncidents(UUID id, Integer limit){
        if (limit == null){
            limit = 1;
        }
        if (limit < 1 || limit > 100){
            throw new InvalidRangeException(limit.toString());
        }
        return incidentRepository.findByMonitorIdOrderByOpenedAtDesc(id, Limit.of(limit));
    }
}
