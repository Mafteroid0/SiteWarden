package ru.mafteroid;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.mafteroid.service.MonitorSchedulingService;

@Component
public class Scheduler {
    private final MonitorSchedulingService monitorSchedulingService;

    public Scheduler(MonitorSchedulingService monitorSchedulingService) {
        this.monitorSchedulingService = monitorSchedulingService;
    }

    @Scheduled(fixedRate = 5000)
    public void schedule() {
        monitorSchedulingService.scheduleDueMonitors();
    }
}
