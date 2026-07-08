ALTER TABLE payments
    ADD COLUMN checkout_attempt INT NOT NULL DEFAULT 1;
