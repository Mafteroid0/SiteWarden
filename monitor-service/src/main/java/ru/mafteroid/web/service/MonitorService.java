package ru.mafteroid.web.service;

import org.springframework.stereotype.Service;
import ru.mafteroid.entity.Monitor;
import ru.mafteroid.repository.MonitorRepository;
import ru.mafteroid.web.dto.CreateMonitorRequest;
import ru.mafteroid.web.dto.MonitorResponse;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class MonitorService {
    private static final Set<Integer> ALLOWED_INTERVALS =
            Set.of(30, 60, 300);

    private final MonitorRepository monitorRepository;

    public MonitorService(MonitorRepository monitorRepository) {
        this.monitorRepository = monitorRepository;
    }

    private void validateUrl(String url) {
        try {
            URI uri = new URI(url);

            String scheme = uri.getScheme();
//TODO: custom exceptions
            if (scheme == null ||
                    !(scheme.equalsIgnoreCase("http")
                            || scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("Only HTTP/HTTPS allowed");
            }

            if (uri.getHost() == null) {
                throw new IllegalArgumentException("URL must contain a host");
            }

            if (uri.getUserInfo() != null) {
                throw new IllegalArgumentException("URL credentials are not allowed");
            }

        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL", e);
        }
    }

    public MonitorResponse createMonitor(CreateMonitorRequest createMonitorRequest) {

        if (!ALLOWED_INTERVALS.contains(createMonitorRequest.intervalSeconds())){
            throw new IllegalArgumentException("Invalid interval " + createMonitorRequest.intervalSeconds());
        }
        validateUrl(createMonitorRequest.url());

        if (createMonitorRequest.timeoutMs() >= createMonitorRequest.intervalSeconds() * 1000) {
            throw new IllegalArgumentException(
                    "Timeout must be less than interval"
            );
        }


        Monitor monitor = Monitor.create(createMonitorRequest.name(),
                createMonitorRequest.url(),
                createMonitorRequest.intervalSeconds(),
                createMonitorRequest.timeoutMs());

        Monitor savedMonitor = monitorRepository.save(monitor);

        return new MonitorResponse(savedMonitor);
    }

    public MonitorResponse findMonitorById(UUID monitorId) {
        Monitor monitor = monitorRepository.findById(monitorId).orElseThrow(
                () -> new IllegalArgumentException("Monitor with id " + monitorId + " not found")
        );
        return new MonitorResponse(monitor);
    }
    public List<MonitorResponse> findAllMonitors() {
        return monitorRepository.findAll().stream()
                .map(MonitorResponse::new)
                .toList();
    }
}
