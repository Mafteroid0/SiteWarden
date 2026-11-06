package ru.mafteroid.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.mafteroid.events.CheckCompletedEvent;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "check_results")
public class CheckResult {
    @Id
    @Column(name = "check_id", nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "monitor_id", nullable = false)
    private UUID monitorId;

    @Size(max = 32)
    @NotNull
    @Column(name = "check_type", nullable = false, length = 32)
    private String checkType;

    @NotNull
    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @NotNull
    @Column(name = "completed_at", nullable = false)
    private OffsetDateTime completedAt;

    @NotNull
    @Column(name = "successful", nullable = false)
    private Boolean successful = false;

    @Column(name = "http_status")
    private Integer httpStatus;

    @NotNull
    @Column(name = "duration_ms", nullable = false)
    private Long durationMs;

    @Size(max = 64)
    @Column(name = "error_code", length = 64)
    private String errorCode;

    protected CheckResult() {}

    public CheckResult(CheckCompletedEvent event) {
        this.id = event.checkId();
        this.monitorId = event.monitorId();
        this.checkType = event.checkType().name();
        this.startedAt = event.startedAt().atOffset(ZoneOffset.UTC);
        this.completedAt = event.completedAt().atOffset(ZoneOffset.UTC);
        this.successful = event.successful();
        this.httpStatus = event.httpStatus();
        this.durationMs = event.durationMs();
        this.errorCode = event.errorCode();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getMonitorId() {
        return monitorId;
    }

    public void setMonitorId(UUID monitorId) {
        this.monitorId = monitorId;
    }

    public String getCheckType() {
        return checkType;
    }

    public void setCheckType(String checkType) {
        this.checkType = checkType;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(OffsetDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(OffsetDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Boolean getSuccessful() {
        return successful;
    }

    public void setSuccessful(Boolean successful) {
        this.successful = successful;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

}