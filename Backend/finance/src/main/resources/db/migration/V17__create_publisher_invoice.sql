-- Billing service owns this table in its own database (adstudio_billing).
-- publisher_id references a user in the IAM service; io_id references an
-- insertion order in the Media/IO service. No cross-service DB foreign keys —
-- plain indexed id columns.

CREATE TABLE publisher_invoice (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    publisher_id    BIGINT        NOT NULL,
    io_id           BIGINT        NOT NULL,
    invoice_amount  DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    delivered_value DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    variance_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    received_date   DATE          NULL,
    status          VARCHAR(20)   NOT NULL DEFAULT 'RECEIVED',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE INDEX idx_publisher_invoice_publisher ON publisher_invoice (publisher_id);
CREATE INDEX idx_publisher_invoice_io        ON publisher_invoice (io_id);
CREATE INDEX idx_publisher_invoice_status    ON publisher_invoice (status);
