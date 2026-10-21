CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash TEXT,
    identity_provider VARCHAR(40) NOT NULL DEFAULT 'LOCAL',
    identity_subject VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_users_status
        CHECK (status IN ('ACTIVE', 'DISABLED'))
);

CREATE UNIQUE INDEX ux_users_external_identity
    ON users(identity_provider, identity_subject)
    WHERE identity_subject IS NOT NULL;

CREATE TABLE gyms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    timezone VARCHAR(80) NOT NULL DEFAULT 'Africa/Accra',
    currency CHAR(3) NOT NULL DEFAULT 'GHS',
    status VARCHAR(20) NOT NULL DEFAULT 'PROVISIONING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_gyms_status CHECK (
        status IN ('PROVISIONING', 'ACTIVE', 'SUSPENDED', 'CLOSED')
    )
);

CREATE TABLE gym_users (
    gym_id UUID NOT NULL REFERENCES gyms(id),
    user_id UUID NOT NULL REFERENCES users(id),
    role VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (gym_id, user_id),
    CONSTRAINT ck_gym_users_role
        CHECK (role IN ('OWNER', 'MANAGER', 'STAFF')),
    CONSTRAINT ck_gym_users_status
        CHECK (status IN ('ACTIVE', 'INVITED', 'DISABLED'))
);

CREATE TABLE tenant_database_registry (
    gym_id UUID PRIMARY KEY REFERENCES gyms(id),
    database_key VARCHAR(100) NOT NULL UNIQUE,
    secret_reference TEXT NOT NULL,
    migration_version VARCHAR(100),
    provisioned_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE saas_subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    gym_id UUID NOT NULL REFERENCES gyms(id),
    plan_code VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'TRIAL',
    billing_period_start DATE,
    billing_period_end DATE,
    next_billing_date DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_saas_status CHECK (
        status IN ('TRIAL', 'ACTIVE', 'PAST_DUE', 'CANCELLED')
    )
);

CREATE INDEX ix_saas_subscriptions_gym
    ON saas_subscriptions(gym_id, status);
