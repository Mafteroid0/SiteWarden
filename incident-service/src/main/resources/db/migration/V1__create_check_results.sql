CREATE TABLE check_results (
       check_id     UUID PRIMARY KEY,
       monitor_id   UUID NOT NULL,
       check_type   VARCHAR(32) NOT NULL,
       started_at   TIMESTAMPTZ NOT NULL,
       completed_at TIMESTAMPTZ NOT NULL,
       successful   BOOLEAN NOT NULL,
       http_status  INTEGER,
       duration_ms  BIGINT NOT NULL CHECK (duration_ms >= 0),
       error_code   VARCHAR(64)
);

CREATE INDEX idx_check_results_monitor_time
    ON check_results (monitor_id, completed_at DESC);