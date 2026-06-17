ALTER TABLE registrations
    ADD COLUMN payment_access_token_hash    VARCHAR(64),
    ADD COLUMN payment_access_token_expires_at TIMESTAMP;
