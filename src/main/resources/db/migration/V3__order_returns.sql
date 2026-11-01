-- Return & Refund workflow (docs/06). order_returns tracks each return request and its resolved
-- decision; refund_ledger holds one immutable refund row per approved (REFUNDED) return.
CREATE TABLE order_returns (
    id                UUID PRIMARY KEY,
    order_id          UUID NOT NULL REFERENCES orders(id),
    customer_id       UUID NOT NULL,
    reason_code       VARCHAR(32)  NOT NULL,
    reason            VARCHAR(512),
    requested_amount  NUMERIC(18,2) NOT NULL,
    currency          VARCHAR(3)   NOT NULL,
    status            VARCHAR(16)  NOT NULL,       -- REFUNDED|UNDER_REVIEW|DENIED
    decision          VARCHAR(16)  NOT NULL,       -- AUTO_REFUND|REVIEW|DENY
    risk_score        INT          NOT NULL,
    idempotency_key   VARCHAR(128),
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL,
    version           BIGINT       NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX ux_order_returns_active ON order_returns(order_id)
    WHERE status IN ('REFUNDED','UNDER_REVIEW');   -- at most one active return per order
CREATE INDEX idx_order_returns_customer ON order_returns(customer_id);

CREATE TABLE refund_ledger (
    id           UUID PRIMARY KEY,
    return_id    UUID NOT NULL UNIQUE REFERENCES order_returns(id),
    order_id     UUID NOT NULL,
    amount       NUMERIC(18,2) NOT NULL,
    currency     VARCHAR(3)   NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL
);
