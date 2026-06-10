CREATE TABLE media_plan (
    plan_id                INT PRIMARY KEY AUTO_INCREMENT,
    brief_id               INT NOT NULL,
    planner_id             INT NOT NULL,
    total_budget_allocated DECIMAL(15,2),
    channel_mix            TEXT,
    start_date             DATE,
    end_date               DATE,
    status                 VARCHAR(20) DEFAULT 'Draft',
    created_at             DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at             DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE media_line_item (
    line_item_id           INT PRIMARY KEY AUTO_INCREMENT,
    plan_id                INT NOT NULL,
    channel                VARCHAR(20),
    publisher              VARCHAR(100),
    format                 VARCHAR(50),
    planned_impressions    INT,
    planned_budget         DECIMAL(15,2),
    cpm                    DECIMAL(8,2),
    flight_start           DATE,
    flight_end             DATE,
    status                 VARCHAR(20) DEFAULT 'Planned',
    created_at             DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at             DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_lineitem_plan FOREIGN KEY (plan_id) REFERENCES media_plan(plan_id)
);

CREATE TABLE insertion_order (
    io_id                  INT PRIMARY KEY AUTO_INCREMENT,
    line_item_id           INT NOT NULL,
    publisher_id           INT NOT NULL,
    order_date             DATE,
    start_date             DATE,
    end_date               DATE,
    committed_impressions  INT,
    order_value            DECIMAL(15,2),
    status                 VARCHAR(20) DEFAULT 'Sent',
    created_at             DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at             DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_io_lineitem FOREIGN KEY (line_item_id) REFERENCES media_line_item(line_item_id)
);