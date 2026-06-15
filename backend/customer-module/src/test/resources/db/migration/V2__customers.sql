-- customers table — Sprint 1 Core Banking

CREATE TABLE IF NOT EXISTS customers (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    type        VARCHAR(20)  NOT NULL,
    document    VARCHAR(14)  NOT NULL,
    email       VARCHAR(255) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by  VARCHAR(100) NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_by  VARCHAR(100) NOT NULL,
    CONSTRAINT uk_customers_document UNIQUE (document)
);

CREATE INDEX IF NOT EXISTS idx_customers_document ON customers (document);
