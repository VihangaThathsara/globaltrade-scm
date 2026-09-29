CREATE DATABASE IF NOT EXISTS globaltrade_scm
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE globaltrade_scm;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS shipment_items;
DROP TABLE IF EXISTS customs_documents;
DROP TABLE IF EXISTS vendor_evaluations;
DROP TABLE IF EXISTS stock_receipts;
DROP TABLE IF EXISTS shipments;
DROP TABLE IF EXISTS inventory_items;
DROP TABLE IF EXISTS alerts;
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS performance_metrics;
DROP TABLE IF EXISTS automation_runs;
DROP TABLE IF EXISTS user_groups;
DROP TABLE IF EXISTS vendors;
DROP TABLE IF EXISTS warehouses;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE users (
    username VARCHAR(60) PRIMARY KEY,
    password_hash VARCHAR(160) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE user_groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(60) NOT NULL,
    group_name VARCHAR(60) NOT NULL,
    CONSTRAINT uq_user_group UNIQUE (username, group_name),
    CONSTRAINT fk_user_group_user FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
    INDEX idx_user_groups_username (username)
) ENGINE=InnoDB;

CREATE TABLE vendors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vendor_code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(140) NOT NULL,
    country VARCHAR(80) NOT NULL,
    contact_email VARCHAR(140),
    performance_score DOUBLE NULL DEFAULT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    INDEX idx_vendor_status (status)
) ENGINE=InnoDB;

CREATE TABLE warehouses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    country VARCHAR(80) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE inventory_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(40) NOT NULL UNIQUE,
    item_name VARCHAR(140) NOT NULL,
    quantity INT NOT NULL,
    reorder_level INT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_inventory_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    INDEX idx_inventory_stock (active, quantity, reorder_level),
    INDEX idx_inventory_product (item_name),
    INDEX idx_inventory_updated (updated_at)
) ENGINE=InnoDB;

CREATE TABLE stock_receipts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    inventory_item_id BIGINT NOT NULL,
    vendor_id BIGINT NOT NULL,
    quantity_received INT NOT NULL,
    received_at DATETIME NOT NULL,
    received_by VARCHAR(60) NOT NULL,
    reviewed BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_receipt_inventory FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id),
    CONSTRAINT fk_receipt_vendor FOREIGN KEY (vendor_id) REFERENCES vendors(id),
    INDEX idx_receipt_inventory (inventory_item_id, received_at),
    INDEX idx_receipt_vendor (vendor_id, received_at),
    INDEX idx_receipt_review (reviewed, received_at)
) ENGINE=InnoDB;

CREATE TABLE shipments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tracking_number VARCHAR(40) NOT NULL UNIQUE,
    carrier VARCHAR(100) NOT NULL,
    origin VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    status VARCHAR(25) NOT NULL,
    eta DATETIME NOT NULL,
    route_score INT NOT NULL DEFAULT 90,
    created_by VARCHAR(60) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    INDEX idx_shipment_status_eta (status, eta),
    INDEX idx_shipment_created_at (created_at)
) ENGINE=InnoDB;

CREATE TABLE shipment_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shipment_id BIGINT NOT NULL,
    inventory_item_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    CONSTRAINT fk_shipment_item_shipment FOREIGN KEY (shipment_id) REFERENCES shipments(id) ON DELETE CASCADE,
    CONSTRAINT fk_shipment_item_inventory FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id),
    INDEX idx_shipment_items_shipment (shipment_id),
    INDEX idx_shipment_items_inventory (inventory_item_id)
) ENGINE=InnoDB;

CREATE TABLE customs_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shipment_id BIGINT NOT NULL,
    type VARCHAR(80) NOT NULL,
    reference_no VARCHAR(60) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    deadline DATETIME NOT NULL,
    submitted_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_customs_shipment FOREIGN KEY (shipment_id) REFERENCES shipments(id) ON DELETE CASCADE,
    INDEX idx_customs_status_deadline (status, deadline)
) ENGINE=InnoDB;

CREATE TABLE vendor_evaluations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vendor_id BIGINT NOT NULL,
    inventory_item_id BIGINT NULL,
    stock_receipt_id BIGINT NULL,
    supply_quantity INT NOT NULL DEFAULT 0,
    score DOUBLE NOT NULL,
    notes VARCHAR(500),
    reviewed_by VARCHAR(60) NOT NULL,
    evaluated_at DATETIME NOT NULL,
    CONSTRAINT fk_vendor_evaluation_vendor FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE CASCADE,
    CONSTRAINT fk_vendor_evaluation_inventory FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id) ON DELETE SET NULL,
    CONSTRAINT fk_vendor_evaluation_receipt FOREIGN KEY (stock_receipt_id) REFERENCES stock_receipts(id) ON DELETE SET NULL,
    CONSTRAINT uq_vendor_evaluation_receipt UNIQUE (stock_receipt_id),
    INDEX idx_vendor_evaluation_inventory (inventory_item_id, evaluated_at),
    INDEX idx_vendor_evaluation_vendor (vendor_id, evaluated_at)
) ENGINE=InnoDB;

CREATE TABLE alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(30) NOT NULL,
    severity VARCHAR(15) NOT NULL,
    message VARCHAR(500) NOT NULL,
    entity_ref VARCHAR(80),
    resolved BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    resolved_at DATETIME NULL,
    INDEX idx_alert_open (resolved, created_at),
    INDEX idx_alert_entity (type, entity_ref, resolved)
) ENGINE=InnoDB;

CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(60) NOT NULL,
    operation VARCHAR(120) NOT NULL,
    component_name VARCHAR(120) NOT NULL,
    success BOOLEAN NOT NULL,
    detail_message VARCHAR(500),
    created_at DATETIME NOT NULL,
    INDEX idx_audit_created (created_at),
    INDEX idx_audit_user (username, created_at)
) ENGINE=InnoDB;

CREATE TABLE performance_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    component_name VARCHAR(120) NOT NULL,
    operation_name VARCHAR(120) NOT NULL,
    duration_ms BIGINT NOT NULL,
    success BOOLEAN NOT NULL,
    recorded_at DATETIME NOT NULL,
    INDEX idx_performance_recorded (recorded_at)
) ENGINE=InnoDB;

CREATE TABLE automation_runs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    automation_name VARCHAR(120) NOT NULL,
    status VARCHAR(20) NOT NULL,
    duration_ms BIGINT NOT NULL,
    message VARCHAR(500),
    executed_at DATETIME NOT NULL,
    INDEX idx_automation_executed (executed_at)
) ENGINE=InnoDB;

INSERT INTO users (username, password_hash, full_name, active) VALUES
('amal perera',      '120000$PvQNVsFU4jhKEa357ZxWzg==$xdA/+U9N9Ah4a89ZsBxdFnaoDljeYzMHU1/A6HIA00M=', 'Amal Perera', TRUE),
('sahan jayasinghe', '120000$+aV9e/Zfh7mCh4YuJryW7A==$Xv7DG9NfccHtZ6Ee77LpCHUUO3UkMYwH9/R3BQPtQVo=', 'Sahan Jayasinghe', TRUE),
('nethmi silva',     '120000$vP7+wG6SaSx0+xGgdGVe9w==$dSHCqPsUMB8mAF6SwtcwMs3nj7bGdREnslVC4x5V+VE=', 'Nethmi Silva', TRUE),
('dinuka fernando',  '120000$3hwQM0P7cBqLpKu51M7rAw==$QBsenymKCuPVRJNmQBARilU7l7xRn5/v4nolfXL6sQM=', 'Dinuka Fernando', TRUE),
('kasun wijesinghe', '120000$FpQQ79K2nXTKcpL3UNvvUA==$V8eXdyFdVkH1Jy+mJaB54qlZBbvEKq56RlG1KyxlRo4=', 'Kasun Wijesinghe', TRUE);

INSERT INTO user_groups (username, group_name) VALUES
('amal perera', 'ADMIN'),
('sahan jayasinghe', 'LOGISTICS_COORDINATOR'),
('nethmi silva', 'WAREHOUSE_MANAGER'),
('dinuka fernando', 'CUSTOMS_OFFICER'),
('kasun wijesinghe', 'VENDOR_USER');

INSERT INTO warehouses (warehouse_code, name, country) VALUES
('WH-CMB', 'Colombo Distribution Hub', 'Sri Lanka'),
('WH-KAT', 'Katunayake Air Cargo Hub', 'Sri Lanka'),
('WH-HMB', 'Hambantota Port Warehouse', 'Sri Lanka'),
('WH-SIN', 'Singapore Regional Hub', 'Singapore'),
('WH-DXB', 'Dubai Transit Hub', 'United Arab Emirates');
