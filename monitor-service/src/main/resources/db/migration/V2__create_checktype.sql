ALTER TABLE monitors
    ADD COLUMN check_type VARCHAR(32) NOT NULL DEFAULT 'HTTP';

ALTER TABLE monitors
    ADD CONSTRAINT chk_monitor_check_type
        CHECK (check_type IN ('HTTP', 'TCP', 'TLS_CERTIFICATE'));