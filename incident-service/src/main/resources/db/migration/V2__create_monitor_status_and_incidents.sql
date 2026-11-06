CREATE TABLE monitor_status (
        monitor_id  UUID PRIMARY KEY,
        status  VARCHAR(4) NOT NULL CHECK (status IN ('UP', 'DOWN')),
        last_checked_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE incidents (
       id   UUID PRIMARY KEY,
       monitor_id   UUID NOT NULL,
       opened_at    TIMESTAMPTZ NOT NULL,
       resolved_at  TIMESTAMPTZ
);

CREATE UNIQUE INDEX uq_incidents_one_open_per_monitor
    ON incidents (monitor_id)
    WHERE resolved_at IS NULL;