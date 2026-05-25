CREATE TABLE public.checkin_tokens
(
    id                        uuid                           NOT NULL,
    created_at                timestamp(6) without time zone NOT NULL,
    event_id                  uuid                           NOT NULL,
    expires_at                timestamp(6) without time zone NOT NULL,
    generated_by_organizer_id uuid                           NOT NULL,
    token                     character varying(16)          NOT NULL
);

CREATE TABLE public.event_capacity
(
    event_id     uuid    NOT NULL,
    max_capacity integer,
    reserved     integer NOT NULL
);

CREATE TABLE public.event_categories
(
    id          uuid                   NOT NULL,
    event_id    uuid                   NOT NULL,
    max_age     integer,
    min_age     integer,
    modality_id uuid,
    name        character varying(100) NOT NULL
);

CREATE TABLE public.event_images
(
    id              uuid                           NOT NULL,
    created_at      timestamp(6) without time zone NOT NULL,
    display_order   integer                        NOT NULL,
    event_id        uuid                           NOT NULL,
    image_public_id character varying(200)         NOT NULL,
    image_url       character varying(500)         NOT NULL
);

CREATE TABLE public.event_modalities
(
    id                  uuid                   NOT NULL,
    capacity            integer                NOT NULL,
    distance            numeric(10, 3)         NOT NULL,
    distance_unit       character varying(15)  NOT NULL,
    event_id            uuid                   NOT NULL,
    name                character varying(100) NOT NULL,
    price               numeric(10, 2)         NOT NULL,
    registered_count    integer DEFAULT 0      NOT NULL,
    price_without_shirt numeric(10, 2),
    CONSTRAINT event_modalities_distance_unit_check CHECK (((distance_unit)::text = ANY
                                                            ((ARRAY ['KM'::character varying, 'MI'::character varying])::text[])))
);

CREATE TABLE public.events
(
    id                    uuid                           NOT NULL,
    cover_image_public_id character varying(200),
    cover_image_url       character varying(500),
    created_on            timestamp(6) without time zone NOT NULL,
    description           text,
    event_date            timestamp(6) without time zone NOT NULL,
    city                  character varying(100),
    country               character varying(100),
    latitude              numeric(10, 7),
    longitude             numeric(10, 7),
    place                 character varying(500)         NOT NULL,
    name                  character varying(200)         NOT NULL,
    registration_end      timestamp(6) without time zone NOT NULL,
    registration_start    timestamp(6) without time zone NOT NULL,
    status                character varying(30)          NOT NULL,
    updated_on            timestamp(6) without time zone NOT NULL,
    created_by            uuid                           NOT NULL,
    version               integer DEFAULT 0              NOT NULL,
    reminder_sent_at      timestamp(6) without time zone,
    waiver_template       text,
    CONSTRAINT events_status_check CHECK (((status)::text = ANY
                                           ((ARRAY ['DRAFT'::character varying, 'PUBLISHED'::character varying, 'REGISTRATION_OPEN'::character varying, 'REGISTRATION_CLOSED'::character varying, 'IN_PROGRESS'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying])::text[])))
);

CREATE TABLE public.organizer_profile
(
    id                  uuid                           NOT NULL,
    created_at          timestamp(6) without time zone NOT NULL,
    description         character varying(500),
    facebook            character varying(150),
    instagram           character varying(150),
    logo_public_id      character varying(200),
    logo_url            character varying(500),
    organization_name   character varying(120)         NOT NULL,
    updated_at          timestamp(6) without time zone NOT NULL,
    verification_status character varying(30)          NOT NULL,
    verified_at         timestamp(6) without time zone,
    website             character varying(150),
    user_id             uuid,
    CONSTRAINT organizer_profile_verification_status_check CHECK (((verification_status)::text = ANY
                                                                   ((ARRAY ['NOT_SUBMITTED'::character varying, 'PENDING_REVIEW'::character varying, 'VERIFIED'::character varying, 'REJECTED'::character varying])::text[])))
);

CREATE TABLE public.participant_profiles
(
    id                      uuid                           NOT NULL,
    blood_type              character varying(10)          NOT NULL,
    created_at              timestamp(6) without time zone NOT NULL,
    emergency_contact_name  character varying(120)         NOT NULL,
    emergency_contact_phone character varying(20)          NOT NULL,
    medical_conditions      character varying(500),
    shirt_size              character varying(20)          NOT NULL,
    updated_at              timestamp(6) without time zone NOT NULL,
    user_id                 uuid,
    gender                  character varying(20),
    phone                   character varying(20),
    CONSTRAINT participant_profiles_blood_type_check CHECK (((blood_type)::text = ANY
                                                             ((ARRAY ['A_POSITIVE'::character varying, 'A_NEGATIVE'::character varying, 'B_POSITIVE'::character varying, 'B_NEGATIVE'::character varying, 'AB_POSITIVE'::character varying, 'AB_NEGATIVE'::character varying, 'O_POSITIVE'::character varying, 'O_NEGATIVE'::character varying])::text[]))),
    CONSTRAINT participant_profiles_gender_check CHECK (((gender)::text = ANY
                                                         ((ARRAY ['FEMENIL'::character varying, 'VARONIL'::character varying, 'OTRO'::character varying])::text[]))),
    CONSTRAINT participant_profiles_shirt_size_check CHECK (((shirt_size)::text = ANY
                                                             ((ARRAY ['SIZE_XS'::character varying, 'SIZE_S'::character varying, 'SIZE_M'::character varying, 'SIZE_L'::character varying, 'SIZE_XL'::character varying, 'SIZE_XXL'::character varying])::text[])))
);

CREATE TABLE public.registrations
(
    id                 uuid                           NOT NULL,
    bib_number         integer,
    cancelled_at       timestamp(6) without time zone,
    event_id           uuid                           NOT NULL,
    kit_picked_up      boolean DEFAULT false          NOT NULL,
    kit_picked_up_at   timestamp(6) without time zone,
    participant_id     uuid                           NOT NULL,
    payment_method     character varying(255),
    registered_at      timestamp(6) without time zone NOT NULL,
    status             character varying(255)         NOT NULL,
    ticket_code        character varying(9)           NOT NULL,
    modality_id        uuid,
    waiver_accepted_at timestamp(6) without time zone,
    waiver_text        text,
    wants_shirt        boolean DEFAULT true           NOT NULL,
    category_id        uuid
);

CREATE TABLE public.roles
(
    id   uuid                  NOT NULL,
    role character varying(30) NOT NULL,
    CONSTRAINT roles_role_check CHECK (((role)::text = ANY
                                        ((ARRAY ['ROLE_ADMIN'::character varying, 'ROLE_USER'::character varying, 'ROLE_ORGANIZER'::character varying, 'ROLE_PARTICIPANT'::character varying])::text[])))
);

CREATE TABLE public.user_roles
(
    user_id uuid NOT NULL,
    role_id uuid NOT NULL
);

CREATE TABLE public.users
(
    id               uuid                           NOT NULL,
    city             character varying(100),
    country          character varying(50),
    external_number  character varying(255),
    internal_number  character varying(255),
    neighborhood     character varying(255),
    state            character varying(50),
    street           character varying(255),
    zip_code         character varying(10),
    created_at       timestamp(6) without time zone NOT NULL,
    email            character varying(255)         NOT NULL,
    last_access      timestamp(6) without time zone NOT NULL,
    password_hash    character varying(255)         NOT NULL,
    birth_date       date,
    first_name       character varying(255),
    gender           character varying(255),
    last_name        character varying(255),
    phone_number     character varying(255),
    second_last_name character varying(255)
);

ALTER TABLE ONLY public.checkin_tokens
    ADD CONSTRAINT checkin_tokens_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.event_capacity
    ADD CONSTRAINT event_capacity_pkey PRIMARY KEY (event_id);

ALTER TABLE ONLY public.event_categories
    ADD CONSTRAINT event_categories_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.event_images
    ADD CONSTRAINT event_images_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.event_modalities
    ADD CONSTRAINT event_modalities_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.events
    ADD CONSTRAINT events_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.organizer_profile
    ADD CONSTRAINT organizer_profile_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.participant_profiles
    ADD CONSTRAINT participant_profiles_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.registrations
    ADD CONSTRAINT registrations_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);

ALTER TABLE ONLY public.organizer_profile
    ADD CONSTRAINT uk8gly8kkbxlt5p2m0oq59c7ndc UNIQUE (user_id);

ALTER TABLE ONLY public.registrations
    ADD CONSTRAINT ukb0d9dy5qoqofunanfy8rl9v4k UNIQUE (ticket_code);

ALTER TABLE ONLY public.checkin_tokens
    ADD CONSTRAINT ukb55g59rm15sbqk60ygxhrwok UNIQUE (token);

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT ukg50w4r0ru3g9uf6i6fr4kpro8 UNIQUE (role);

ALTER TABLE ONLY public.registrations
    ADD CONSTRAINT ukjfludtls0h0ut49hrelrmhq3c UNIQUE (event_id, participant_id);

ALTER TABLE ONLY public.participant_profiles
    ADD CONSTRAINT uknlp23xxe6neswvnr0cwstibfr UNIQUE (user_id);

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_pkey PRIMARY KEY (user_id, role_id);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.organizer_profile
    ADD CONSTRAINT fkafd6anrfmpvwv9t1w6sgo206g FOREIGN KEY (user_id) REFERENCES public.users (id);

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT fkh8ciramu9cc9q3qcqiv4ue8a6 FOREIGN KEY (role_id) REFERENCES public.roles (id);

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT fkhfh9dx7w3ubf1co1vdev94g3f FOREIGN KEY (user_id) REFERENCES public.users (id);

ALTER TABLE ONLY public.events
    ADD CONSTRAINT fkmpv90a1lsx9lcxsj7xjcvvsxg FOREIGN KEY (created_by) REFERENCES public.users (id);

ALTER TABLE ONLY public.participant_profiles
    ADD CONSTRAINT fkqt4gsljb9vjhuoewquqq4u1tx FOREIGN KEY (user_id) REFERENCES public.users (id);
