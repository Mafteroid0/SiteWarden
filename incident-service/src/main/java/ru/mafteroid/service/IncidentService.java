package ru.mafteroid.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mafteroid.entity.CheckResult;
import ru.mafteroid.events.CheckCompletedEvent;
import ru.mafteroid.repository.IncidentRepository;

@Service
public class IncidentService {
    private final IncidentRepository incidentRepository;

    public IncidentService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }


    @Transactional
    public void process(CheckCompletedEvent event) {
        CheckResult checkResult = new CheckResult(event);

        incidentRepository.save(checkResult);
    }
}
