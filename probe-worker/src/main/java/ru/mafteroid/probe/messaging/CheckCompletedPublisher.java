package ru.mafteroid.probe.messaging;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.mafteroid.events.CheckCompletedEvent;

@Component
public class CheckCompletedPublisher {
    private final KafkaTemplate<String, CheckCompletedEvent> kafkaTemplate;
    private static final Logger log = LoggerFactory.getLogger(CheckCompletedPublisher.class);

    public CheckCompletedPublisher(KafkaTemplate<String, CheckCompletedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(CheckCompletedEvent event) {
        kafkaTemplate.send(
                "check.completed",
                event.monitorId().toString(),
                event
        ).whenComplete((result,exception)->{
            if (exception != null) {
                log.error("Publishing failed: " + event.checkId(), exception);
            } else{
                log.info("Published checkId={}, partition={}, offset={} ",
                        event.checkId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
