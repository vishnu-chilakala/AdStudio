-- Billing service owns this table in its own database (adstudio_billing).
-- advertiser_id / campaign_brief_id reference records owned by OTHER services
-- (Advertiser/Campaign), so there is NO DB-level foreign key across the service
-- boundary — they are plain indexed id columns. Referential integrity across
-- services is handled at the application/contract level.

CREATE TABLE client_invoice (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    advertiser_id     BIGINT        NOT NULL,
    campaign_brief_id BIGINT        NULL,
    billing_period    VARCHAR(20)   NULL,
    invoice_amount    DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    agency_commission DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    net_billable      DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    issued_date       DATE          NULL,
    status            VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE INDEX idx_client_invoice_advertiser ON client_invoice (advertiser_id);
CREATE INDEX idx_client_invoice_brief      ON client_invoice (campaign_brief_id);
CREATE INDEX idx_client_invoice_status     ON client_invoice (status);
CREATE INDEX idx_client_invoice_issued     ON client_invoice (issued_date);
