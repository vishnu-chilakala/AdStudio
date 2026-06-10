-- ============================================
-- V3: Pacing alert table
-- ============================================

CREATE TABLE pacing_alert (
    alert_id       INT PRIMARY KEY AUTO_INCREMENT,
    line_item_id   INT NOT NULL,
    alert_type     VARCHAR(30) NOT NULL,
    alert_date     DATE,
    pacing_percent DECIMAL(6,2),
    status         VARCHAR(20) DEFAULT 'Open',
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);