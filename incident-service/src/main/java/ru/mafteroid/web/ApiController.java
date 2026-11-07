package ru.mafteroid.web;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.mafteroid.entity.CheckResult;
import ru.mafteroid.entity.Incident;
import ru.mafteroid.entity.MonitorStatus;
import ru.mafteroid.service.ApiService;

import java.util.List;
import java.util.UUID;

@RestController
public class ApiController {


    private final ApiService apiService;

    public ApiController(ApiService apiService) {
        this.apiService = apiService;
    }

    @GetMapping("/{id}/status")
    public MonitorStatus getStatus(@PathVariable UUID id){
        return apiService.findById(id);
    }

    @GetMapping("/{id}/checks")
    public List<CheckResult> getChecks(@PathVariable UUID id,
                                       @RequestParam(required = false) Integer limit){
        return apiService.getChecks(id, limit);
    }

    @GetMapping("/{id}/incidents")
    public List<Incident> getIncidents(@PathVariable UUID id,
                                    @RequestParam(required = false) Integer limit){
        return apiService.getIncidents(id, limit);
    }
}
