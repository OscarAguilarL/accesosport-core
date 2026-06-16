CREATE TABLE stripe_webhook_events (
    event_id     VARCHAR(255) PRIMARY KEY,
    event_type   VARCHAR(255) NOT NULL,
    status       VARCHAR(30)  NOT NULL,
    received_at  TIMESTAMP    NOT NULL,
    processed_at TIMESTAMP,
    last_error   VARCHAR(1000)
);

ALTER TABLE payments
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
