ALTER TABLE registrations ALTER COLUMN participant_id DROP NOT NULL;

ALTER TABLE registrations
    ADD COLUMN participant_email        varchar(255),
    ADD COLUMN participant_first_name   varchar(255),
    ADD COLUMN participant_last_name    varchar(255),
    ADD COLUMN participant_phone        varchar(50),
    ADD COLUMN shirt_size               varchar(20),
    ADD COLUMN blood_type               varchar(20),
    ADD COLUMN emergency_contact_name   varchar(255),
    ADD COLUMN emergency_contact_phone  varchar(50),
    ADD COLUMN medical_conditions       text;

ALTER TABLE registrations DROP CONSTRAINT ukjfludtls0h0ut49hrelrmhq3c;

CREATE UNIQUE INDEX uq_registrations_event_participant
    ON registrations (event_id, participant_id)
    WHERE participant_id IS NOT NULL;

CREATE UNIQUE INDEX uq_registrations_event_email
    ON registrations (event_id, participant_email)
    WHERE participant_email IS NOT NULL;
