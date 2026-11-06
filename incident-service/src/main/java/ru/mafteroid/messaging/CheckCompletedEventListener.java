package ru.mafteroid.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.mafteroid.events.CheckCompletedEvent;
import ru.mafteroid.service.IncidentService;

@Component
public class CheckCompletedEventListener {


    private final IncidentService incidentService;

    public CheckCompletedEventListener(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @KafkaListener(
            topics = "check.completed",
            groupId = "incident-service"
    )
    public void listen(CheckCompletedEvent event) {
        incidentService.process(event);


    }
}
