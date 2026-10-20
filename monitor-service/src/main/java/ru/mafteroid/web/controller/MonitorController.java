package ru.mafteroid.web.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.mafteroid.web.dto.CreateMonitorRequest;
import ru.mafteroid.web.dto.MonitorResponse;
import ru.mafteroid.web.service.MonitorService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/monitors")
public class MonitorController {

    private final MonitorService monitorService;

    public MonitorController(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @GetMapping("/{id}")
    public MonitorResponse findById(@PathVariable UUID id) {
        return monitorService.findMonitorById(id);
    }

    @GetMapping
    public List<MonitorResponse> findAll() {
        return monitorService.findAllMonitors();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MonitorResponse createMonitor(@RequestBody @Valid CreateMonitorRequest createMonitorRequest) {
        return monitorService.createMonitor(createMonitorRequest);
    }
}
