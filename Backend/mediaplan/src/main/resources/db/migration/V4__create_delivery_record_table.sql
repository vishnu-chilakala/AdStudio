-- ============================================
-- V4: Delivery record table (TEMPORARY — for standalone pacing testing).
-- Will be replaced by Dev 4's delivery-service during integration.
-- ============================================

CREATE TABLE delivery_record (
    delivery_id           INT PRIMARY KEY AUTO_INCREMENT,
    line_item_id          INT NOT NULL,
    reporting_date        DATE,
    delivered_impressions INT,
    spend                 DECIMAL(15,2),
    created_at            DATETIME DEFAULT CURRENT_TIMESTAMP
);