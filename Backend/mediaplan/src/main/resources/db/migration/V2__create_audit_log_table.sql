CREATE TABLE audit_log (
    audit_id     INT PRIMARY KEY AUTO_INCREMENT,
    user_id      INT,
    action       VARCHAR(100) NOT NULL,
    entity_type  VARCHAR(50),
    entity_id    INT,
    timestamp    DATETIME DEFAULT CURRENT_TIMESTAMP
);