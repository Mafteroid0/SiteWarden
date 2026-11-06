package ru.mafteroid.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "monitor_status")
public class MonitorStatus {
    @Id
    @Column(name = "monitor_id", nullable = false)
    private UUID id;

    @Size(max = 4)
    @NotNull
    @Column(name = "status", nullable = false, length = 4)
    private String status;

    @NotNull
    @Column(name = "last_checked_at", nullable = false)
    private OffsetDateTime lastCheckedAt;

    protected MonitorStatus() {}

    public MonitorStatus(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getLastCheckedAt() {
        return lastCheckedAt;
    }

    public void setLastCheckedAt(OffsetDateTime lastCheckedAt) {
        this.lastCheckedAt = lastCheckedAt;
    }

}