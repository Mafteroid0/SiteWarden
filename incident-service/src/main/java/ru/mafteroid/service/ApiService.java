package ru.mafteroid.service;

import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import ru.mafteroid.entity.CheckResult;
import ru.mafteroid.entity.Incident;
import ru.mafteroid.entity.MonitorStatus;
import ru.mafteroid.repository.CheckResultRepository;
import ru.mafteroid.repository.IncidentRepository;
import ru.mafteroid.repository.MonitorStatusRepository;

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

    public MonitorStatus findById(UUID monitorId){
        return monitorStatusRepository.findMonitorStatusBy(monitorId);
    }

    public List<CheckResult> getChecks(UUID id, Integer limit){
        if (limit == null || limit < 1 || limit > 100){
            limit = 10;
        }
        return checkResultRepository.findByMonitorIdOrderByCompletedAtDesc(id, Limit.of(limit));
    }

    public List<Incident> getIncidents(UUID id, Integer limit){
        if (limit == null || limit < 1 || limit > 100){
            limit = 1;
        }
        return incidentRepository.findByMonitorIdOrderByOpenedAtDesc(id, Limit.of(limit));
    }
}
