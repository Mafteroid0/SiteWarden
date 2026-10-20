CREATE TABLE monitors (
    id UUID PRIMARY KEY,

    name VARCHAR(100) NOT NULL,
    url VARCHAR(2048) NOT NULL,

    interval_seconds INT NOT NULL,
    timeout_ms INT NOT NULL,

    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    next_check_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_monitor_name
      CHECK (length(trim(name)) > 0),

    CONSTRAINT chk_monitor_url
      CHECK (length(trim(url)) > 0),

    CONSTRAINT chk_monitor_interval
      CHECK (interval_seconds IN (30, 60, 300)),

    CONSTRAINT chk_monitor_timeout
      CHECK (timeout_ms BETWEEN 1000 AND 10000),

    CONSTRAINT chk_monitor_schedule
      CHECK (NOT enabled OR next_check_at IS NOT NULL)
);

CREATE INDEX idx_monitors_due
    ON monitors (next_check_at)
    WHERE enabled = TRUE;