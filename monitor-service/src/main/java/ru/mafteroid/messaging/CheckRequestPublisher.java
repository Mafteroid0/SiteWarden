package ru.mafteroid.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.mafteroid.events.CheckRequestedEvent;

@Component
public class CheckRequestPublisher {

    private final KafkaTemplate<String, CheckRequestedEvent> kafkaTemplate;
    private static final Logger log =
            LoggerFactory.getLogger(CheckRequestPublisher.class);

    public CheckRequestPublisher(KafkaTemplate<String, CheckRequestedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(CheckRequestedEvent event){
        kafkaTemplate.send("check.requested", event.monitorId().toString(), event)
                .whenComplete((result,exception)->{
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
