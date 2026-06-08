CREATE TABLE invitation_tokens
(
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token           UUID         NOT NULL UNIQUE,
    email           VARCHAR(255) NOT NULL,
    reason          TEXT,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_by      UUID         NOT NULL REFERENCES users (id),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    used_at         TIMESTAMP,
    used_by_user_id UUID REFERENCES users (id),
    revoked_at      TIMESTAMP
);
CREATE INDEX idx_invitation_tokens_token ON invitation_tokens (token);
CREATE INDEX idx_invitation_tokens_email ON invitation_tokens (email);
