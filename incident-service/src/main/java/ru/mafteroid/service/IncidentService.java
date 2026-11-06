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
        CheckResult checkResult = new CheckResult(event);
        checkResultRepository.save(checkResult);
        updateStatus(checkResult);



    }

    @Transactional
    public void updateStatus(CheckResult checkResult) {

        MonitorStatus monitorStatus = monitorStatusRepository.findById(checkResult.getMonitorId()).
                orElse(null);

        String newStatus = checkResult.getSuccessful() ? "UP" : "DOWN";

        if (monitorStatus == null) {
            monitorStatus = new MonitorStatus(checkResult.getMonitorId());
            monitorStatus.setStatus(newStatus);
        }
        if (!newStatus.equals(monitorStatus.getStatus())) {
            changeIncident(monitorStatus.getId());
            monitorStatus.setStatus(newStatus);
        }
        else {
            return;
        }

        monitorStatus.setLastCheckedAt(OffsetDateTime.now());
        monitorStatusRepository.save(monitorStatus);
    }


    @Transactional
    public void changeIncident(UUID monitorId) {

        Incident incident = incidentRepository.findByMonitorIdAndResolvedAtIsNull(monitorId)
                .orElse(null);
        if (incident == null ) {
            createIncident(monitorId);
        }else{
            incident.setResolvedAt(OffsetDateTime.now());
            incidentRepository.save(incident);
        }




    }

    @Transactional
    protected void createIncident(UUID monitorId) {
        Incident incident = new Incident(monitorId, OffsetDateTime.now());
        incidentRepository.save(incident);

    }
}
