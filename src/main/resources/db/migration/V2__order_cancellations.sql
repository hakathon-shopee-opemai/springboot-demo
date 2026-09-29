-- Saga log for order cancellations (docs/05). Persists state + step outcomes for
-- idempotency and audit. saga_steps holds a JSON array of {name,status} as TEXT.
CREATE TABLE order_cancellations (
    id               UUID PRIMARY KEY,
    order_id         UUID NOT NULL UNIQUE REFERENCES orders(id),
    status           VARCHAR(16)  NOT NULL,   -- IN_PROGRESS|COMPLETED|COMPENSATED|FAILED
    reason           VARCHAR(512),
    requested_by     VARCHAR(128),
    saga_steps       TEXT         NOT NULL DEFAULT '[]',
    idempotency_key  VARCHAR(128),
    created_at       TIMESTAMPTZ  NOT NULL,
    updated_at       TIMESTAMPTZ  NOT NULL,
    version          BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_order_cancellations_order_id ON order_cancellations(order_id);
