CREATE TABLE payments (
    id UUID PRIMARY KEY,
    registration_id UUID NOT NULL UNIQUE,
    stripe_session_id VARCHAR(255) NOT NULL UNIQUE,
    stripe_payment_intent_id VARCHAR(255),
    stripe_refund_id VARCHAR(255),
    base_amount NUMERIC(10, 2) NOT NULL,
    service_fee_amount NUMERIC(10, 2) NOT NULL,
    amount_total NUMERIC(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'mxn',
    payment_method VARCHAR(20),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    confirmed_at TIMESTAMP,
    refunded_at TIMESTAMP,
    CONSTRAINT fk_payment_registration FOREIGN KEY (registration_id) REFERENCES registrations (id)
);
