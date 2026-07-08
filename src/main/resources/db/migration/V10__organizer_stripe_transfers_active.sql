ALTER TABLE organizer_profile
    ADD COLUMN stripe_transfers_active BOOLEAN NOT NULL DEFAULT FALSE;
