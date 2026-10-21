package ru.mafteroid.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "monitors")
public class Monitor {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 100)
    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 2048)
    @NotNull
    @Column(name = "url", nullable = false, length = 2048)
    private String url;

    @NotNull
    @Column(name = "interval_seconds", nullable = false)
    private Integer intervalSeconds;

    @NotNull
    @Column(name = "timeout_ms", nullable = false)
    private Integer timeoutMs;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "next_check_at")
    private Instant nextCheckAt;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Monitor() {}

    public static Monitor create(
            String name,
            String url,
            int intervalSeconds,
            int timeoutMs
    ) {
        Monitor monitor = new Monitor();

        Instant now = Instant.now();

        monitor.name = name;
        monitor.url = url;
        monitor.intervalSeconds = intervalSeconds;
        monitor.timeoutMs = timeoutMs;

        monitor.enabled = true;
        monitor.nextCheckAt = now;
        monitor.createdAt = now;
        monitor.updatedAt = now;

        return monitor;
    }

    public void pause() {
        this.enabled = false;
        this.nextCheckAt = null;
        this.updatedAt = Instant.now();
    }
    public void resume() {
        if (Boolean.TRUE.equals(this.enabled)) {
            return;
        }
        this.enabled = true;
        this.nextCheckAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(Integer intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }

    public Integer getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(Integer timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Instant getNextCheckAt() {
        return nextCheckAt;
    }

    public void setNextCheckAt(Instant nextCheckAt) {
        this.nextCheckAt = nextCheckAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

}