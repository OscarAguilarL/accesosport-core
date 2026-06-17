ALTER TABLE payments
    ADD COLUMN refund_error    VARCHAR(500),
    ADD COLUMN refund_attempts INT NOT NULL DEFAULT 0;
