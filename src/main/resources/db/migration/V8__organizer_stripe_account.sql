ALTER TABLE organizer_profile
    ADD COLUMN stripe_account_id VARCHAR(255),
    ADD COLUMN stripe_onboarding_completed BOOLEAN NOT NULL DEFAULT FALSE;
