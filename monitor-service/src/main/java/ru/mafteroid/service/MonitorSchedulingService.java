package ru.mafteroid.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mafteroid.entity.Monitor;
import ru.mafteroid.events.CheckRequestedEvent;
import ru.mafteroid.messaging.CheckRequestPublisher;
import ru.mafteroid.messaging.CheckRequestedEventFactory;
import ru.mafteroid.repository.MonitorRepository;

import java.time.Instant;
import java.util.List;

@Service
public class MonitorSchedulingService {

    private final MonitorRepository monitorRepository;
    private final CheckRequestPublisher checkRequestPublisher;

    public MonitorSchedulingService(MonitorRepository monitorRepository, CheckRequestPublisher checkRequestPublisher) {
        this.monitorRepository = monitorRepository;
        this.checkRequestPublisher = checkRequestPublisher;
    }
    @Transactional
    public void scheduleDueMonitors(){
        List<Monitor> monitors = monitorRepository.findAllByEnabledTrueAndNextCheckAtLessThanEqual(Instant.now());
        for (Monitor monitor : monitors) {
            CheckRequestedEvent event = CheckRequestedEventFactory.fromMonitor(monitor);
            checkRequestPublisher.publish(event);
            //TODO: outbox
            monitor.setNextCheckAt(
                    Instant.now().plusSeconds(monitor.getIntervalSeconds())
            );
        }
    }
}
