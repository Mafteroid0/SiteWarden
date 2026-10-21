package ru.mafteroid.web.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mafteroid.web.dto.CreateMonitorRequest;
import ru.mafteroid.web.dto.MonitorResponse;
import ru.mafteroid.web.dto.UpdateMonitorStateRequest;
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

    @PatchMapping("/{id}")
    public MonitorResponse updateMonitor(@PathVariable UUID id, @RequestBody @Valid UpdateMonitorStateRequest updateMonitorStateRequest) {
        return monitorService.updateMonitor(id, updateMonitorStateRequest.enabled());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMonitor(@PathVariable UUID id) {
        monitorService.deleteMonitor(id);
        return ResponseEntity.noContent().build();
    }
}
