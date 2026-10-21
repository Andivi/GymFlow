CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_e164 VARCHAR(20) NOT NULL,
    email VARCHAR(320),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    sms_opt_in BOOLEAN NOT NULL DEFAULT FALSE,
    whatsapp_opt_in BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_members_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX ix_members_status ON members(status);
CREATE INDEX ix_members_phone ON members(phone_e164);

CREATE TABLE membership_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(120) NOT NULL,
    description TEXT,
    price_minor BIGINT NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'GHS',
    duration_days INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_plan_price CHECK (price_minor >= 0),
    CONSTRAINT ck_plan_duration CHECK (duration_days > 0),
    CONSTRAINT ck_plan_status
        CHECK (status IN ('ACTIVE', 'ARCHIVED'))
);
