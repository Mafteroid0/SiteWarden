package ru.mafteroid.probe.messaging;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.mafteroid.events.CheckCompletedEvent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Component
public class CheckCompletedPublisher {
    private final KafkaTemplate<String, CheckCompletedEvent> kafkaTemplate;
    private static final Logger log = LoggerFactory.getLogger(CheckCompletedPublisher.class);

    public CheckCompletedPublisher(KafkaTemplate<String, CheckCompletedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(CheckCompletedEvent event) throws InterruptedException{
        try {
            var result = kafkaTemplate
                    .send("check.completed", event.monitorId().toString(), event)
                    .get(10, TimeUnit.SECONDS);

            log.info("Published checkId={}, partition={}, offset={}",
                    event.checkId(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());

        } catch (ExecutionException | java.util.concurrent.TimeoutException e) {
            throw new IllegalStateException(
                    "Kafka did not confirm checkId=" + event.checkId(), e
            );
        }
}}
