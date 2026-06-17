-- ledger_entries_stub — Sprint 1 double-entry placeholder (transfer-money)

CREATE TABLE IF NOT EXISTS ledger_entries_stub (
    id             UUID PRIMARY KEY,
    transfer_id    UUID         REFERENCES transfers (id),
    account_id     UUID         NOT NULL REFERENCES accounts (id),
    entry_type     VARCHAR(10)  NOT NULL,
    amount         NUMERIC(19, 2) NOT NULL,
    currency       CHAR(3)      NOT NULL,
    correlation_id UUID         NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_ledger_entries_stub_entry_type CHECK (entry_type IN ('DEBIT', 'CREDIT'))
);

CREATE INDEX IF NOT EXISTS idx_ledger_entries_stub_transfer_id ON ledger_entries_stub (transfer_id);
CREATE INDEX IF NOT EXISTS idx_ledger_entries_stub_account_id ON ledger_entries_stub (account_id);
