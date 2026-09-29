-- order-service schema (owned by Flyway; Hibernate runs in validate mode).

CREATE SEQUENCE IF NOT EXISTS order_number_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE customers (
    id                     UUID PRIMARY KEY,
    email                  VARCHAR(320) NOT NULL UNIQUE,
    full_name              VARCHAR(255) NOT NULL,
    phone                  VARCHAR(32),
    account_status         VARCHAR(16)  NOT NULL,
    tier                   VARCHAR(16)  NOT NULL,
    lifetime_order_count   BIGINT       NOT NULL DEFAULT 0,
    lifetime_gmv_amount    NUMERIC(18,2) NOT NULL DEFAULT 0,
    lifetime_gmv_currency  VARCHAR(3)   NOT NULL DEFAULT 'USD',
    last_order_at          TIMESTAMPTZ,
    created_at             TIMESTAMPTZ  NOT NULL,
    updated_at             TIMESTAMPTZ  NOT NULL,
    version                BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE orders (
    id                   UUID PRIMARY KEY,
    order_number         VARCHAR(32)  NOT NULL UNIQUE,
    customer_id          UUID         NOT NULL REFERENCES customers(id),
    status               VARCHAR(16)  NOT NULL,
    currency             VARCHAR(3)   NOT NULL,
    subtotal_amount      NUMERIC(18,2) NOT NULL,
    shipping_fee_amount  NUMERIC(18,2) NOT NULL,
    total_amount         NUMERIC(18,2) NOT NULL,
    ship_line1           VARCHAR(255),
    ship_city            VARCHAR(128),
    ship_country         VARCHAR(64),
    ship_postal_code     VARCHAR(32),
    payment_method       VARCHAR(16)  NOT NULL,
    risk_score           INTEGER,
    risk_level           VARCHAR(8),
    risk_decision        VARCHAR(8),
    risk_assessment_id   UUID,
    risk_assessed_at     TIMESTAMPTZ,
    idempotency_key      VARCHAR(128) UNIQUE,
    created_at           TIMESTAMPTZ  NOT NULL,
    updated_at           TIMESTAMPTZ  NOT NULL,
    version              BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE order_lines (
    id                 UUID PRIMARY KEY,
    order_id           UUID          NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id         VARCHAR(64)   NOT NULL,
    product_name       VARCHAR(255),
    unit_price_amount  NUMERIC(18,2) NOT NULL,
    quantity           INTEGER       NOT NULL,
    line_total_amount  NUMERIC(18,2) NOT NULL
);

CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_order_lines_order_id ON order_lines(order_id);
