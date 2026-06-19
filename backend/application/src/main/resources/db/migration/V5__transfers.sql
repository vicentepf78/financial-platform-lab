-- transfers table — Sprint 1 Core Banking (transfer-money)

CREATE TABLE IF NOT EXISTS transfers (
    id                       UUID PRIMARY KEY,
    origin_account_id        UUID         NOT NULL REFERENCES accounts (id),
    destination_account_id   UUID         NOT NULL REFERENCES accounts (id),
    amount                   NUMERIC(19, 2) NOT NULL,
    currency                 VARCHAR(3)   NOT NULL DEFAULT 'BRL',
    status                   VARCHAR(20)  NOT NULL,
    correlation_id           UUID         NOT NULL,
    idempotency_key          VARCHAR(100),
    created_at               TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by               VARCHAR(100) NOT NULL,
    CONSTRAINT uk_transfers_idempotency_key UNIQUE (idempotency_key)
);

CREATE INDEX IF NOT EXISTS idx_transfers_origin ON transfers (origin_account_id);
CREATE INDEX IF NOT EXISTS idx_transfers_correlation ON transfers (correlation_id);
